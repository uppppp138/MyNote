package com.mynote.util;


import com.mynote.domain.po.Note;

public class NoteContext {
    
    private static final ThreadLocal<Note> OLD_NOTE_HOLDER = new ThreadLocal<>();
    
    public static void setOldNote(Note note) {
        OLD_NOTE_HOLDER.set(note);
    }
    
    public static Note getOldNote() {
        return OLD_NOTE_HOLDER.get();
    }
    
    public static void clear() {
        OLD_NOTE_HOLDER.remove();
    }
}