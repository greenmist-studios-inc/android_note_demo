package com.greenmiststudios.demolibrary;

/**
 * User: geoffpowell
 * Date: 12/9/15
 */
public class NoteAction {

    public Long _id;
    private Long noteID;

    public NoteAction() {}

    public NoteAction(Note note) {
        noteID = note.getID();
    }

}
