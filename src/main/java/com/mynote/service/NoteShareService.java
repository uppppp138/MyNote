package com.mynote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mynote.domain.po.NoteShare;
import com.mynote.domain.vo.NoteShareVO;
import com.mynote.domain.vo.ShareInfoVO;

public interface NoteShareService extends IService<NoteShare> {
    ShareInfoVO shareNote(Long id, Integer expireHours);

    NoteShareVO getShareNote(String shareCode);
}
