package com.mynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mynote.domain.po.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
