package com.greenmiststudios.androiddemo.service;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.androiddemo.activity.ExternalNoteActivity;
import com.greenmiststudios.androiddemo.activity.NoteActivity;
import com.greenmiststudios.androiddemo.helper.DAO;
import com.greenmiststudios.demolibrary.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * User: geoffpowell
 * Date: 12/9/15
 */
@TargetApi(Build.VERSION_CODES.M)
public class DirectShareService extends ChooserTargetService {

    @Override
    public List<ChooserTarget> onGetChooserTargets(ComponentName targetActivityName, IntentFilter matchedFilter) {
        ComponentName componentName = new ComponentName(getPackageName(),
                ExternalNoteActivity.class.getCanonicalName());
        ArrayList<ChooserTarget> targets = new ArrayList<>();
        List<Note> recentNotes = DAO.getLastActiveNotes(this);
        for (Note note : recentNotes) {
            Bundle extras = new Bundle();
            extras.putLong(NoteActivity.EXTRA_NOTE, note.getID());
            extras.putBoolean(ExternalNoteActivity.CATEGORY_APPEND, true);
            Icon icon = Icon.createWithResource(this, R.drawable.share_icon);
            targets.add(new ChooserTarget(
                    note.getTitle(),
                    icon,
                    0.5f,
                    componentName,
                    extras));
        }
        return targets;
    }
}
