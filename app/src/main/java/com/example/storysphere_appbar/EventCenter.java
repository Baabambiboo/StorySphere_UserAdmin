package com.example.storysphere_appbar;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public final class EventCenter {

    // ชื่อ action กลาง
    public static final String ACTION_WRITING_CHANGED = "com.example.storysphere.WRITING_CHANGED";

    // payload keys (เผื่ออยากใช้ต่อ)
    public static final String EXTRA_WRITING_ID = "writing_id";
    public static final String EXTRA_CHANGE_TYPE = "change_type"; // like / view / bookmark

    private EventCenter() {}

    /** ส่ง broadcast ว่า writing มีการเปลี่ยนค่าแล้ว */
    public static void notifyChanged(Context ctx, int writingId, String changeType) {
        Intent i = new Intent(ACTION_WRITING_CHANGED);
        i.putExtra(EXTRA_WRITING_ID, writingId);
        i.putExtra(EXTRA_CHANGE_TYPE, changeType);
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(i);
    }
}
