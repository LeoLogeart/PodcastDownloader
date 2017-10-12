package com.dl.podcastgrossestetes;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MediaPlayerReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                MediaPlayerObservableObject.getInstance().updateValue(null);
            }
        }

}
