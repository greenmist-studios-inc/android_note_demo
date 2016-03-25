package com.greenmiststudios.androiddemo.event;

/**
 * User: geoffpowell
 * Date: 12/14/15
 */
public class RefreshDataEvent {
    public boolean deleted;
    private boolean updated;

    public RefreshDataEvent(boolean deleted) {
        this.deleted = deleted;
        this.updated = true;
    }
}
