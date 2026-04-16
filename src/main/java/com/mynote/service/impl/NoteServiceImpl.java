package com.mynote.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.mynote.common.BusinessException;
import com.mynote.common.RedisKey;
import com.mynote.domain.dto.NoteDTO;
import com.mynote.domain.dto.PageDTO;
import com.mynote.domain.po.Category;
import com.mynote.domain.po.Note;
import com.mynote.domain.po.NoteHistory;
import com.mynote.domain.query.NotePageQuery;
import com.mynote.domain.vo.NoteHistoryVO;
import com.mynote.domain.vo.NoteVO;
import com.mynote.mapper.NoteHistoryMapper;
import com.mynote.mapper.NoteMapper;
import com.mynote.service.CategoryService;
import com.mynote.service.NoteService;
import com.mynote.util.CollUtils;
import com.mynote.util.MarkdownUtils;
import com.mynote.util.PdfFontUtils;
import com.mynote.util.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mynote.common.RedisKey.HOT_KEY_PREFIX;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {
    private final NoteMapper noteMapper;
    private final CategoryService categoryService;
    private final StringRedisTemplate redisTemplate;
    private final NoteHistoryMapper noteHistoryMapper;



    @Transactional
    @Override
    public Note createNote(NoteDTO noteDTO) {
        //1.异常判断
        if (noteDTO == null ) {
            throw new BusinessException(400, "笔记信息不能为空");
        }
        //2.笔记标题为空，生成默认标题
        if (noteDTO.getTitle() == null || noteDTO.getTitle().length() == 0) {
            String defaultTitle = "笔记_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            noteDTO.setTitle(defaultTitle);
        }
        //3.判断categoryId是否为null
        List<Note> notes ;
        if (noteDTO.getCategoryId() == 0 || noteDTO.getCategoryId() == null) {
            noteDTO.setCategoryId(null);
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getUserId, UserContext.getUser())
                    .eq(Note::getTitle, noteDTO.getTitle())
                    .isNull(Note::getCategoryId);
            notes = noteMapper.selectList(wrapper);
        }else{
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getUserId, UserContext.getUser())
                    .eq(Note::getTitle, noteDTO.getTitle())
                    .eq(Note::getCategoryId, noteDTO.getCategoryId());
            notes= noteMapper.selectList(wrapper);
        }
        if (!notes.isEmpty()) {
            throw new BusinessException(400, "分类下的笔记已存在，请更换主题再进行添加");
        }
        //3.笔记标题不重复，添加笔记
        Note note = Note.builder()
                .title(noteDTO.getTitle())
                .content(noteDTO.getContent())
                .userId(UserContext.getUser())
                .categoryId(noteDTO.getCategoryId())
                .viewCount(0)
                .isDeleted(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        noteMapper.insert(note);
        return note;
    }

    @Override
    @Transactional
    public NoteDTO updateNote(NoteDTO noteDTO) {
        //1.异常判断
        if (noteDTO == null || noteDTO.getId() == null) {
            throw new BusinessException(400, "笔记信息不能为空");
        }
        //2.查看当前用户是否有该笔记
        Note note = noteMapper.selectById(noteDTO.getId());
        if (note == null) {
            throw new BusinessException(403, "笔记不存在");
        }
        if (!note.getUserId().equals(UserContext.getUser())) {
            throw new BusinessException(403, "您没有权限修改该笔记");
        }
        //3.更新笔记
        //3.1 标题不为空，更新标题
        if (StrUtil.isNotBlank(noteDTO.getTitle())) {
            //查看当前标题的其他笔记是否存在
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getUserId, UserContext.getUser())
                    .eq(Note::getTitle, noteDTO.getTitle())
                    .ne(Note::getId, noteDTO.getId());

            if (noteDTO.getCategoryId() != 0 && noteDTO.getCategoryId() != null) {
                //有分类
                wrapper.eq(Note::getCategoryId, noteDTO.getCategoryId());
            }else{
                //默认分类
                wrapper.isNull(Note::getCategoryId);
            }

            Long count = noteMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(400, "该分类下已存在同名笔记");
            }
            note.setTitle(noteDTO.getTitle());
        }
        //3.2 内容不为空，更新内容
        if (StrUtil.isNotBlank(noteDTO.getContent())) {
            note.setContent(noteDTO.getContent());
        }
        //3.3 分类id不为空，更新分类id
        if (noteDTO.getCategoryId() != null) {
            //TODO OpenFeign 4.查看当前用户的当前分类是否存在
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getId, noteDTO.getCategoryId())
                    .eq(Category::getUserId, UserContext.getUser());
            Category category = categoryService.getOne(wrapper);
            // 4.1当前分类不存在
            if (category == null) {
                throw new BusinessException(400, "当前分类不存在，不能修改");
            }
            note.setCategoryId(noteDTO.getCategoryId());
        }
        note.setUpdateTime(LocalDateTime.now());
        //5.修改笔记
        String key = RedisKey.USER_NOTE_VIEW_COUNT_PREFIX + note.getId();
        Long newCount = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        note.setViewCount(note.getViewCount() + newCount.intValue());
        noteMapper.updateById(note);
        return NoteDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .categoryId(note.getCategoryId())
                .build();

    }


    @Override
    @Transactional
    public Note getDetailById(Long id) {
        //1.查看当前用户是否有该笔记
        Note note = getNoteByIdAngUserId(id);
        if (note == null) {
            throw new BusinessException(403, "当前用户下的笔记不存在");
        }
        //2.更新笔记view_count+1——数据库更新
        /**note.setViewCount(note.getViewCount() + 1);
         Note newNote = note.builder()
         .id(note.getId())
         .viewCount(note.getViewCount())
         .build();
         noteMapper.updateById(newNote);*/
        //2.1更新redis中的key对应的count
        String key = RedisKey.USER_NOTE_VIEW_COUNT_PREFIX + id;
        Long newCount = redisTemplate.opsForValue().increment(key);//如果redis中没有key,会自动添加并返回初始值0
        //2.2重置过期时间，防止大量冷门笔记堆积
        redisTemplate.expire(key, 7, TimeUnit.DAYS);

        //3.更新返回笔记的viewcount
        note.setViewCount(note.getViewCount() + newCount.intValue());

        //4.返回笔记详情
        return note;
    }

    @Override
    public PageDTO<Note> listNote(NotePageQuery notePageQuery) {
        Page<Note> result = lambdaQuery()
                .eq(Note::getUserId, UserContext.getUser())//用户隔离
                .eq(Note::getIsDeleted, 0)
                .like(StrUtil.isNotBlank(notePageQuery.getKeyword()), Note::getTitle, notePageQuery.getKeyword())//标题的模糊匹配
                .eq(notePageQuery.getCategoryId()!=null && notePageQuery.getCategoryId() > 0, Note::getCategoryId, notePageQuery.getCategoryId())//分类的id
                .isNull(notePageQuery.getCategoryId() != null && notePageQuery.getCategoryId() == -1, Note::getCategoryId)//没有分类id就isnull
                .page(notePageQuery.toMpPage("update_time", false));
        return PageDTO.of(result);
    }

    @Override
    @Transactional
    public void restoreNote(Long id) {
        //1.查看当前用户是否有该笔记
        if (id == null) {
            throw new BusinessException("笔记id无效");
        }
        //2.查询当前用户回收站内有无该笔记
        //拦截器只处理实体字段映射的 Lambda 表达式，不处理纯字符串
        noteMapper.restoreNote(id, UserContext.getUser());
    }


    @Override
    @Transactional
    public void softDeleteNote(Long id) {
        //1.查看当前用户是否有该笔记
        if (id == null) {
            throw new BusinessException("笔记id无效");
        }
        List<Note> notes = lambdaQuery().eq(Note::getUserId, UserContext.getUser())
                .eq(Note::getIsDeleted, 0)
                .eq(Note::getId, id)
                .list();
        if (notes.isEmpty()) {
            throw new BusinessException("当前用户下该笔记不存在");
        }
        //2.软删除该笔记
        redisTemplate.delete(RedisKey.USER_NOTE_VIEW_COUNT_PREFIX + id);
        String key = HOT_KEY_PREFIX + UserContext.getUser();
        redisTemplate.opsForZSet().remove(key, id.toString());
        lambdaUpdate().eq(Note::getId, id)
                .eq(Note::getIsDeleted, 0)
                .eq(Note::getUserId, UserContext.getUser())
                .set(Note::getIsDeleted, 1)
                .set(Note::getDeleteTime, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional
    public void permanentDeleteNote(Long id) {
        if (id == null) {
            throw new BusinessException("笔记id无效");
        }
        //1.查看当前用户的该笔记是否在回收站
        Note note = noteMapper.getSoftDeleteNote(id, UserContext.getUser());
        if (note == null) {
            throw new BusinessException("当前笔记不在回收站，不能删除");
        }
        String key = HOT_KEY_PREFIX + UserContext.getUser();
        redisTemplate.opsForZSet().remove(key, id.toString());
        //2.删除当前用户下的该笔记
        noteMapper.permanentDeleteNote(id, UserContext.getUser());
    }

    @Override
    @Transactional
    public PageDTO<Note> recycleList(NotePageQuery notePageQuery) {
        Page<Note> result = lambdaQuery()
                .eq(Note::getUserId, UserContext.getUser())//用户隔离
                .eq(Note::getIsDeleted, 1)
                .like(StrUtil.isNotBlank(notePageQuery.getKeyword()), Note::getTitle, notePageQuery.getKeyword())//标题的模糊匹配
                .eq(notePageQuery.getCategoryId()!=null && notePageQuery.getCategoryId() > 0, Note::getCategoryId, notePageQuery.getCategoryId())//分类的id
                .isNull(notePageQuery.getCategoryId() != null && notePageQuery.getCategoryId() == -1, Note::getCategoryId)//没有分类id就isnull
                .page(notePageQuery.toMpPage("update_time", false));
        return PageDTO.of(result);

    }

    @Override
    @Transactional
    public void emptyRecycle() {
        //1.查询当前用户下回收站内所有笔记
        List<Note> notes = lambdaQuery().eq(Note::getUserId, UserContext.getUser())
                .eq(Note::getIsDeleted, 1)
                .list();
        if (notes.isEmpty()) {
            throw new BusinessException("当前用户下回收站内无笔记");
        }
        //2.删除所有笔记
        noteMapper.emptyRecycle(UserContext.getUser());
    }

    private static final int HOT_SIZE = 10;
    private static final int ZSET_MAX_SIZE = 100;     // ZSET 保留前100名
    private static final int EXPIRE_DAYS = 7;

    @Override
    public List<NoteVO> tenHotNote() {
        Long userId = UserContext.getUser();
        String key = HOT_KEY_PREFIX + userId;
        //1.从redis ZSET 中获取前10个笔记id
        Set<String> noteIdsStrs = redisTemplate.opsForZSet().reverseRangeByScore(key, 0, HOT_SIZE - 1);

        //2.批量查询笔记详情并按浏览次数排序
        List<NoteVO> noteVOS = new ArrayList<>();
        if (noteIdsStrs != null && !noteIdsStrs.isEmpty()) {
            List<Long> noteIds = noteIdsStrs.stream().map(Long::parseLong).collect(Collectors.toList());
            noteVOS = lambdaQuery()
                    .eq(Note::getUserId, userId)
                    .eq(Note::getIsDeleted, 0)
                    .in(Note::getId, noteIds)
                    .list()
                    .stream()
                    .map(item -> NoteVO.builder()
                            .id(item.getId())
                            .title(item.getTitle())
                            .viewCount(item.getViewCount())
                            .updateTime(item.getUpdateTime())
                            .userId(UserContext.getUser())
                            .build()
                    ).collect(Collectors.toList());
        }
        if (noteVOS.size() >= HOT_SIZE) {
            //取前十个返回
            return noteVOS.subList(0, HOT_SIZE);
        }
        //4.有效笔记不足10条
        // 4.1 删除redis中该用户的旧热门数据
        redisTemplate.delete(key);
        //4.2 从数据库中查询最新的热门笔记——前100条 按浏览数降序
        List<Note> notes = lambdaQuery()
                .eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, 0)
                .orderByDesc(Note::getViewCount)
                .last("limit " + ZSET_MAX_SIZE)
                .list();
        if (notes.isEmpty()) {
            return CollUtils.emptyList();
        }
        //4.3 批量插入redis
        for (Note note : notes) {
            redisTemplate.opsForZSet().add(key, note.getId().toString(), note.getViewCount().doubleValue());
        }
        redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);

        noteVOS = notes.stream()
                .limit(HOT_SIZE)
                .map(this::toVO)
                .collect(Collectors.toList());
        return noteVOS;
    }

    /**
     * 记录当前用户笔记浏览
     */
    @Override
    @Async
    public void recordView(Long noteId, Long userId) {
        String key = HOT_KEY_PREFIX + userId;
        //1.增加对应的分数
        redisTemplate.opsForZSet().incrementScore(key, noteId.toString(), 1);
        //2.保留前100名
        redisTemplate.opsForZSet().removeRange(key, ZSET_MAX_SIZE, -1);
        //3.设置过期时间（用户七天不活跃，key自动清理）
        redisTemplate.expire(key, EXPIRE_DAYS, TimeUnit.DAYS);
    }

    @Override
    public List<NoteHistoryVO> getHistory(Long id) {
        if (id == null) {
            throw new BusinessException("笔记id无效");
        }
        //1.查看当前笔记是否存在
        Note note = lambdaQuery()
                .eq(Note::getId, id)
                .eq(Note::getUserId, UserContext.getUser())
                .eq(Note::getIsDeleted, 0) //过滤未逻辑删除的
                .one(); //只可能有一个
        if (note == null) {
            throw new BusinessException("当前笔记不存在");
        }
        //2.查询所有的历史版本
        List<NoteHistory> histories = noteHistoryMapper.selectList(
                new LambdaQueryWrapper<NoteHistory>()
                        .eq(NoteHistory::getNoteId, id)
                        .eq(NoteHistory::getUserId, UserContext.getUser())
                        .orderByDesc(NoteHistory::getVersion) //按版本号降序
        );

        return histories.stream()
                .map(item -> NoteHistoryVO.builder()
                        .id(item.getId())
                        .noteId(item.getNoteId())
                        .title(item.getTitle())
                        .content(item.getContent())
                        .version(item.getVersion())
                        .createTime(item.getCreateTime())
                        .userId(UserContext.getUser())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void rollback(Long noteId, Integer version) {
        //1.当前指定的历史版本存不存在
        NoteHistory noteHistory = noteHistoryMapper.selectOne(
                new LambdaQueryWrapper<NoteHistory>()
                        .eq(NoteHistory::getNoteId, noteId)
                        .eq(NoteHistory::getVersion, version)
                        .eq(NoteHistory::getUserId, UserContext.getUser())
        );
        if (noteHistory == null) {
            throw new BusinessException("指定历史版本不存在");
        }
        //2.修改当前的笔记为历史版本，那么当前笔记就成了历史版本
        //2.1先获取当前笔记
        Note note = getNoteByIdAngUserId(noteId);

        //3.将当前笔记保存为历史版本你
        //3.1 获取下一个版本号
        Integer nextVersion = getNextVersion(noteId);
        //3.2 保存为历史版本
        NoteHistory newNoteHistory = NoteHistory.builder()
                .title(note.getTitle())
                .noteId(noteId)
                .version(nextVersion)
                .userId(UserContext.getUser())
                .content(note.getContent())
                .build();
        noteHistoryMapper.insert(newNoteHistory);
        //4.恢复历史版本
        updateById(Note.builder()
                .id(noteId)
                .title(noteHistory.getTitle())
                .content(noteHistory.getContent())
                .build());

    }

    @Override
    public void exportPdf(Long noteId, Long userId, HttpServletResponse response) throws IOException {
        // 1. 获取笔记并校验权限
        Note note = getDetailById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限导出该笔记");
        }

        // 2. Markdown 转 HTML
        String htmlContent = MarkdownUtils.markdownToHtml(note.getContent(), note.getTitle());

        // 3. 设置响应头
        String fileName = note.getTitle() + ".pdf";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType("application/pdf");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedFileName);

        // 4. 生成 PDF 并写入响应流
        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            // 配置中文字体支持 [citation:1]
            HtmlConverter.convertToPdf(htmlContent, pdfDoc,
                    PdfFontUtils.getConverterProperties());
        }
    }
    private Integer getNextVersion(Long noteId) {
        Integer maxVersion = noteHistoryMapper.selectMaxVersion(noteId);
        return (maxVersion == null) ? 1 : maxVersion + 1;
    }


    private Note getNoteByIdAngUserId(Long id) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getId, id)
                .eq(Note::getUserId, UserContext.getUser())
                .eq(Note::getIsDeleted, 0);
        Note note = noteMapper.selectOne(wrapper);
        return note;
    }

    private NoteVO toVO(Note note) {
        if (note == null) return null;
        NoteVO vo = new NoteVO();
        BeanUtils.copyProperties(note, vo);
        return vo;
    }
}




