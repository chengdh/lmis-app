package com.lmis.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.lmis.R;


/**
 * Created by chengdh on 14-7-30.
 */
public class SoundPlayer {
    public static final String TAG = "com.lmis.util.SoundPlayer";

    public static void playBarcodeScanSuccessSound(Context ctx) {
        MediaPlayer mPlayer = MediaPlayer.create(ctx, R.raw.barcode_scan_success);
        mPlayer.start();

    }

    public static void playBarcodeScanErrorSound(Context ctx) {
        MediaPlayer mPlayer = MediaPlayer.create(ctx, R.raw.barcode_scan_error);
        mPlayer.start();
    }
}
