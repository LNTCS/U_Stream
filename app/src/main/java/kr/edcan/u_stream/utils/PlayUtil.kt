package kr.edcan.u_stream

import android.content.Context
import android.content.Intent
import java.util.concurrent.TimeUnit

/**
 * Created by LNTCS on 2016-11-23.
 */

object PlayUtil {

    var playService: Intent? = null

    fun getService(context: Context): Intent {
        if (playService == null) {
            playService = Intent(context, PlayService::class.java)
        }
        return playService!!
    }

    fun  setAction(context: Context, action: String) {
        if (playService == null) {
            playService = Intent(context, PlayService::class.java)
        }
        playService!!.action = action
    }

    fun startService(mContext: Context, action: String) {
        PlayUtil.setAction(mContext, action)
        mContext.startService(PlayUtil.getService(mContext))
    }

    fun parseTime(ms: Long): String {
        val millis = ms
        if (millis >= 3600000) {
            val time = String.format("%d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.HOURS.toHours(TimeUnit.MILLISECONDS.toDays(millis)),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
            return time
        } else {
            val time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)))
            return time
        }
    }
}
