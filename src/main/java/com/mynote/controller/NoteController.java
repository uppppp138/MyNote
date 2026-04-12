package com.mynote.controller;

import com.mynote.common.Result;
import com.mynote.domain.dto.NoteDTO;
import com.mynote.domain.po.Note;
import com.mynote.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "笔记管理")
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    /**
     * 创建笔记
     */
    @PostMapping("/create")
    @Operation(summary = "创建笔记")
    public Result<Note> createNote(@RequestBody NoteDTO noteDTO) {
        return Result.success(noteService.createNote(noteDTO));
    }

    /**
     * 更新笔记
     */
    @PostMapping("/update")
    @Operation(summary = "更新笔记")
    public Result<NoteDTO> updateNote(@RequestBody NoteDTO noteDTO) {
        return Result.success(noteService.updateNote(noteDTO));
    }

    /**
     * 删除笔记
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除笔记")
    public Result deleteNote(@PathVariable Long id) {
        noteService.removeNoteById(id);
        return Result.success();
    }

    /**
     * 查询笔记详情
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "查询笔记详情")
    public Result<Note> getNote(@PathVariable Long id) {
        return Result.success(noteService.getDetailById(id));
    }

    /**
     * 分页查询笔记列表
     */


}
