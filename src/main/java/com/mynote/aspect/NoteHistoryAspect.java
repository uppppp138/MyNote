package com.mynote.aspect;

import com.mynote.domain.dto.NoteDTO;
import com.mynote.domain.po.Note;
import com.mynote.domain.po.NoteHistory;
import com.mynote.mapper.NoteHistoryMapper;
import com.mynote.mapper.NoteMapper;
import com.mynote.util.NoteContext;
import com.mynote.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Aspect
@Component
public class NoteHistoryAspect {

    @Autowired
    private NoteHistoryMapper historyMapper;

    @Autowired
    private NoteMapper noteMapper;

    @Pointcut("execution(* com.mynote.service.NoteService.updateNote(..))")
    public void updateNotePointcut() {}

    @Before("updateNotePointcut()")
    public void beforeUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        NoteDTO noteDTO = (NoteDTO) args[0];

        Note oldNote = noteMapper.selectById(noteDTO.getId());

        if (oldNote != null) {
            // 深拷贝：创建新对象，复制所有属性
            Note copy = new Note();
            BeanUtils.copyProperties(oldNote, copy);
            NoteContext.setOldNote(copy);
            log.info("查询到旧笔记: id={}, title={}, content={}",
                    copy.getId(), copy.getTitle(), copy.getContent());
            log.info("旧笔记副本已存入 ThreadLocal");
        }
    }

    @AfterReturning("updateNotePointcut()")
    @Transactional
    public void afterUpdate(JoinPoint joinPoint) {
        log.info("=== AOP @AfterReturning 执行 ===");

        try {
            Note oldNote = NoteContext.getOldNote();
            log.info("从 ThreadLocal 获取旧笔记: {}", oldNote != null ? oldNote.getId() : "null");

            if (oldNote == null) {
                log.warn("旧笔记为空，跳过保存历史");
                return;
            }

            Object[] args = joinPoint.getArgs();
            NoteDTO noteDTO = (NoteDTO) args[0];

            Note newNote = noteMapper.selectById(noteDTO.getId());
            log.info("查询到新笔记: id={}, title={}, content={}",
                    newNote.getId(), newNote.getTitle(), newNote.getContent());

            if (newNote == null) {
                log.warn("新笔记为空，跳过保存历史");
                return;
            }

            // 判断变化
            boolean isSame = isSameContent(oldNote, newNote);
            log.info("内容是否有变化: {}", !isSame);
            log.info("旧标题: {}, 新标题: {}", oldNote.getTitle(), newNote.getTitle());
            log.info("旧内容: {}, 新内容: {}", oldNote.getContent(), newNote.getContent());
            log.info("旧分类: {}, 新分类: {}", oldNote.getCategoryId(), newNote.getCategoryId());

            if (isSame) {
                log.info("内容无变化，跳过保存历史");
                return;
            }

            Integer nextVersion = getNextVersion(oldNote.getId());
            log.info("下一个版本号: {}", nextVersion);

            NoteHistory history = new NoteHistory();
            history.setNoteId(oldNote.getId());
            history.setTitle(oldNote.getTitle());
            history.setContent(oldNote.getContent());
            history.setVersion(nextVersion);
            history.setUserId(UserContext.getUser());

            int result = historyMapper.insert(history);
            log.info("保存历史版本结果: {}", result > 0 ? "成功" : "失败");

        } catch (Exception e) {
            log.error("保存历史版本失败", e);
        } finally {
            NoteContext.clear();
            log.info("ThreadLocal 已清理");
        }
    }

    private boolean isSameContent(Note oldNote, Note newNote) {
        if (oldNote == null || newNote == null) {
            return false;
        }

        boolean titleSame = equals(oldNote.getTitle(), newNote.getTitle());
        boolean contentSame = equals(oldNote.getContent(), newNote.getContent());
        boolean categorySame = equals(oldNote.getCategoryId(), newNote.getCategoryId());

        // 详细日志
        log.info("titleSame: {}, oldTitle: [{}], newTitle: [{}]", titleSame, oldNote.getTitle(), newNote.getTitle());
        log.info("contentSame: {}, oldContent: [{}], newContent: [{}]", contentSame, oldNote.getContent(), newNote.getContent());
        log.info("categorySame: {}", categorySame);

        return titleSame && contentSame && categorySame;
    }

    private boolean equals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private Integer getNextVersion(Long noteId) {
        Integer maxVersion = historyMapper.selectMaxVersion(noteId);
        return (maxVersion == null) ? 1 : maxVersion + 1;
    }
}