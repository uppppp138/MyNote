package com.mynote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mynote.domain.dto.NoteDTO;
import com.mynote.domain.po.Note;

public interface NoteService extends IService<Note> {
    Note createNote(NoteDTO noteDTO);

    NoteDTO updateNote(NoteDTO noteDTO);

    void removeNoteById(Long id);

    Note getDetailById(Long id);
}
