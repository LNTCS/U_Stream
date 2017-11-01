package kr.edcan.u_stream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager

/**
 * Created by LNTCS on 2016-07-11.
 */
class EarphoneBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
            pause(context)
        }
    }

    internal fun pause(context: Context) {
        PlayService.mediaPlayer.pause()
        PlayUtil.startService(context, PlayService.ACTION_PAUSE)
        PlayService.updateView()
    }
}
