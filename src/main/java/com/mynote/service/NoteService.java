package com.mynote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mynote.domain.dto.NoteDTO;
import com.mynote.domain.dto.PageDTO;
import com.mynote.domain.po.Note;
import com.mynote.domain.query.NotePageQuery;
import com.mynote.domain.vo.NoteHistoryVO;
import com.mynote.domain.vo.NoteVO;

import java.util.List;

public interface NoteService extends IService<Note> {
    Note createNote(NoteDTO noteDTO);

    NoteDTO updateNote(NoteDTO noteDTO);



    Note getDetailById(Long id);

    PageDTO<Note> listNote(NotePageQuery notePageQuery);

    void restoreNote(Long id);

    void softDeleteNote(Long id);

    void permanentDeleteNote(Long id);

    PageDTO<Note> recycleList(NotePageQuery notePageQuery);

    void emptyRecycle();

    List<NoteVO> tenHotNote();

    void recordView(Long noteId, Long userId);

    List<NoteHistoryVO> getHistory(Long id);

    void rollback(Long id, Integer version);
}
