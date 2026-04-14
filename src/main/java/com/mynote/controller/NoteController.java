package com.mynote.controller;

import com.mynote.annotation.Log;
import com.mynote.common.Result;
import com.mynote.domain.dto.NoteDTO;
import com.mynote.domain.dto.PageDTO;
import com.mynote.domain.po.Note;
import com.mynote.domain.query.NotePageQuery;
import com.mynote.domain.vo.NoteHistoryVO;
import com.mynote.domain.vo.NoteVO;
import com.mynote.mapper.NoteMapper;
import com.mynote.service.NoteService;
import com.mynote.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

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
    @Log("创建笔记")
    public Result<Note> createNote(@RequestBody NoteDTO noteDTO) {
        return Result.success(noteService.createNote(noteDTO));
    }

    /**
     * 更新笔记
     */
    @PostMapping("/update")
    @Operation(summary = "更新笔记")
    @Log("更新笔记")
    public Result<NoteDTO> updateNote(@RequestBody NoteDTO noteDTO) {
        noteService.recordView(noteDTO.getId(), UserContext.getUser());
        return Result.success(noteService.updateNote(noteDTO));
    }

    /**
     * 软删除笔记：将笔记移入回收站
     */
    @DeleteMapping("/soft-delete/{id}")
    @Operation(summary = "软删除笔记")
    @Log("软删除笔记")
    public Result softDeleteNote(@PathVariable Long id) {
        noteService.softDeleteNote(id);
        return Result.success("已移入回收站");
    }

    /**
     * 恢复笔记
     */
    @Operation(summary = "恢复笔记")
    @PutMapping("/restore/{id}")
    @Log("恢复笔记")
    public Result restoreNote(@PathVariable Long id) {
        noteService.restoreNote(id);
        return Result.success("恢复成功");
    }

    /**
     * 永久删除笔记
     */
    @DeleteMapping("/permanent-delete/{id}")
    @Operation(summary = "永久删除笔记")
    @Log("永久删除笔记")
    public Result permanentDeleteNote(@PathVariable Long id) {
        noteService.permanentDeleteNote(id);
        return Result.success("删除成功");
    }

    /**
     * 查询笔记详情
     */
    @GetMapping("/get/{id}")
    @Operation(summary = "查询笔记详情")
    @Log("查询笔记详情")
    public Result<Note> getNote(@PathVariable Long id) {
        noteService.recordView(id, UserContext.getUser());
        return Result.success(noteService.getDetailById(id));
    }

    /**
     * 分页查询笔记列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询笔记列表")
    @Log("分页查询笔记列表")
    public Result<PageDTO<Note>> listNote(@RequestBody NotePageQuery notePageQuery) {
        return Result.success(noteService.listNote(notePageQuery));
    }
//    @GetMapping("/list")
//    @Operation(summary = "分页查询笔记列表")
//    public Result<PageDTO<Note>> listNote(
//            @RequestParam(required = false) Long categoryId,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(defaultValue = "1") Integer pageNo,
//            @RequestParam(defaultValue = "10") Integer pageSize,
//            HttpServletRequest request) {
//
//        // 打印所有请求参数
//        System.out.println("=== 所有请求参数 ===");
//        request.getParameterMap().forEach((key, value) -> {
//            System.out.println(key + " = " + Arrays.toString(value));
//        });
//        System.out.println("categoryId from @RequestParam: " + categoryId);
//        System.out.println("keyword from @RequestParam: " + keyword);
//        System.out.println("===================");
//
//        NotePageQuery query = new NotePageQuery();
//        query.setCategoryId(categoryId);
//        query.setKeyword(keyword);
//        query.setPageNo(pageNo);
//        query.setPageSize(pageSize);
//
//        return Result.success(noteService.listNote(query));
//    }

    /**
     * 查询回收站列表
     */
    @PostMapping("recycle-list")
    @Operation(summary = "查询回收站列表")
    @Log("查询回收站列表")
    public Result<PageDTO<Note>> recycleList(@RequestBody NotePageQuery notePageQuery) {
        return Result.success(noteService.recycleList(notePageQuery));
    }

    /**
     * 清空回收站
     */
    @DeleteMapping("/empty-recycle")
    @Operation(summary = "清空回收站")
    @Log("清空回收站")
    public Result emptyRecycle() {
        noteService.emptyRecycle();
        return Result.success("回收站已清空");
    }

    /**
     * 热门笔记推荐10条
     */
    @GetMapping("/hot")
    @Operation(summary = "热门笔记推荐")
    @Log("热门笔记推荐")
    public Result<List<NoteVO>> tenHotNote() {
        return Result.success(noteService.tenHotNote());
    }

    /**
     * 查询笔记的历史版本
     */
    @GetMapping("/history/{id}")
    @Operation(summary = "查询笔记的历史版本")
    @Log("查询笔记的历史版本")
    public Result<List<NoteHistoryVO>> getHistory(@PathVariable Long id) {
        return Result.success(noteService.getHistory(id));
    }

    /**
     * 回滚到指定版本
     */
    @PostMapping("/rollback/{id}/{version}")
    @Operation(summary = "回滚到指定版本")
    @Log("回滚到指定版本")
    public Result rollback(@PathVariable Long id, @PathVariable Integer version) {
        noteService.rollback(id, version);
        return Result.success("回滚成功");
    }
}
