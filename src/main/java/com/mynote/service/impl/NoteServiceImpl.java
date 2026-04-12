package com.mynote.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mynote.common.BusinessException;
import com.mynote.domain.dto.NoteDTO;
import com.mynote.domain.po.Category;
import com.mynote.domain.po.Note;
import com.mynote.mapper.NoteMapper;
import com.mynote.service.CategoryService;
import com.mynote.service.NoteService;
import com.mynote.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {
    private final NoteMapper noteMapper;
    private final CategoryService categoryService;


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
        note.setViewCount(note.getViewCount() + 1);
        noteMapper.updateById(note);
        return NoteDTO.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .categoryId(note.getCategoryId())
                .build();

    }

    @Transactional
    @Override
    public void removeNoteById(Long id) {
        //1.查看当前用户是否有该笔记
        Note note = getNoteByIdAngUserId(id);
        if (note == null) {
            throw new BusinessException(403, "当前用户下的笔记不存在");
        }
        //2.删除笔记
        noteMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Note getDetailById(Long id) {
        //1.查看当前用户是否有该笔记
        Note note = getNoteByIdAngUserId(id);
        if (note == null) {
            throw new BusinessException(403, "当前用户下的笔记不存在");
        }
        //2.更新笔记view_count+1
        note.setViewCount(note.getViewCount() + 1);
        Note newNote = note.builder()
                .id(note.getId())
                .viewCount(note.getViewCount())
                .build();
        noteMapper.updateById(newNote);
        //3.返回笔记详情
        return note;
    }

    private Note getNoteByIdAngUserId(Long id) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getId, id)
                .eq(Note::getUserId, UserContext.getUser());
        Note note = noteMapper.selectOne(wrapper);
        return note;
    }
}
