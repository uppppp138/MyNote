package com.mynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mynote.domain.po.NoteHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NoteHistoryMapper extends BaseMapper<NoteHistory> {
    @Select("SELECT MAX(version) FROM note_history WHERE note_id = #{noteId}")
    Integer selectMaxVersion(Long noteId);
}
