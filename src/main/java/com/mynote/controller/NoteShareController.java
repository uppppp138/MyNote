package com.mynote.controller;

import com.mynote.annotation.Log;
import com.mynote.common.Result;
import com.mynote.domain.vo.NoteShareVO;
import com.mynote.domain.vo.ShareInfoVO;
import com.mynote.service.NoteShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "笔记分享")
@RestController
@RequestMapping("/note-share")
@RequiredArgsConstructor
public class NoteShareController {
    //TODO 删除 note_share表 目前看没什么用

    private final NoteShareService noteShareService;

    /**
     * 生成分享链接
     */
    @PostMapping("/{id}")
    @Operation(summary = "生成分享链接")
    @Log("生成分享链接")
    public Result<ShareInfoVO> shareNote(@PathVariable Long id, @RequestParam(value = "expireHours", defaultValue = "24") Integer expireHours) {
        return Result.success(noteShareService.shareNote(id, expireHours));
    }

    /**
     * 访问分享笔记
     */
    @GetMapping("/noValid/{shareCode}")
    @Operation(summary = "访问分享笔记")
    public Result<NoteShareVO> getShareNote(@PathVariable String shareCode) {
        return Result.success(noteShareService.getShareNote(shareCode));
    }

}
