package com.mynote.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mynote.common.BusinessException;
import com.mynote.common.RedisKey;
import com.mynote.domain.po.Note;
import com.mynote.domain.po.NoteShare;
import com.mynote.domain.vo.NoteShareVO;
import com.mynote.domain.vo.ShareInfoVO;
import com.mynote.mapper.NoteMapper;
import com.mynote.mapper.NoteShareMapper;
import com.mynote.service.NoteService;
import com.mynote.service.NoteShareService;
import com.mynote.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteShareServiceImpl extends ServiceImpl<NoteShareMapper, NoteShare> implements NoteShareService {
    private final NoteMapper noteMapper;
    private final NoteShareMapper noteShareMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public ShareInfoVO shareNote(Long noteId,Integer expireHours) {
        //1.查看当前用户下是否存在当前笔记
        Note note = noteMapper.selectOne(
                new LambdaQueryWrapper<Note>()
                        .eq(Note::getId, noteId)
                        .eq(Note::getUserId, UserContext.getUser())
                        .eq(Note::getIsDeleted, 0)
        );
        if (note == null) {
            throw new BusinessException("笔记不存在或无权限分享");
        }
        //2.生成8位随机码
        String shareCode = RandomUtil.randomString(8);

        //3.存入Redis
        String redisKey = RedisKey.NOTE_SHARE_PREFIX + shareCode;
        String redisViewCountKey = RedisKey.NOTE_SHARE_VIEW_COUNT_PREFIX + shareCode;
        if (expireHours > 0) {
            //设置过期时间
            redisTemplate.opsForValue().set(redisKey, String.valueOf(noteId), expireHours, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(redisViewCountKey, "0", expireHours, TimeUnit.DAYS);
        }else{
            throw new BusinessException("过期时间异常");
        }

        //5.构造返回结果
        ShareInfoVO shareInfoVO = ShareInfoVO.builder()
                .shareCode(shareCode)
                .shareUrl(RedisKey.BASE_URL_PREFIX + shareCode)
                .expireTime(LocalDateTime.now().plusHours(expireHours))
                .build();
        return shareInfoVO;
    }

    @Override
    public NoteShareVO getShareNote(String shareCode) {
        if (shareCode == null || shareCode.length() == 0) {
            throw new BusinessException("分享链接无效");
        }
        //1.查询redis获取笔记id
        String redisKey = RedisKey.NOTE_SHARE_PREFIX + shareCode;
        String noteId = redisTemplate.opsForValue().get(redisKey);
        if (noteId == null) {
            throw new BusinessException("分享链接已过期");
        }
        //2.查询笔记
        Note note = noteMapper.selectOne(
                new LambdaQueryWrapper<Note>()
                        .eq(Note::getId, Long.valueOf(noteId))
                        .eq(Note::getIsDeleted, 0)
        );
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        //3.访问次数加一
        redisTemplate.opsForValue().increment(RedisKey.NOTE_SHARE_VIEW_COUNT_PREFIX + shareCode);

        //4.封装返回结果并返回
        return NoteShareVO.builder()
                .title(note.getTitle())
                .content(note.getContent())
                .shareViewCount(Integer.parseInt(redisTemplate.opsForValue().get(RedisKey.NOTE_SHARE_VIEW_COUNT_PREFIX + shareCode)))
                .build();
    }

}
