package com.example.storysphere_appbar;

import android.content.Context;
import android.content.Intent;

public class ReadingMainActivityLauncher {
    public static Intent launch(Context ctx, int writingId) {
        Intent i = new Intent(ctx, ReadingMainActivity.class);
        i.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, writingId);
        return i;
    }
}
