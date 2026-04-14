package com.mynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mynote.domain.po.Note;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    @Update("UPDATE note SET is_deleted = 0, delete_time = null " +
            "WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 1")
    void restoreNote(Long id, Long userId);

    @Delete("DELETE FROM note WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 1")
    void permanentDeleteNote(Long id, Long userId);

    @Select("SELECT * FROM note WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 1")
    Note getSoftDeleteNote(Long id, Long userId);

    @Delete("DELETE FROM note WHERE user_id = #{userId} AND is_deleted = 1")
    void emptyRecycle(Long userId);
}
