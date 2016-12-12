package kr.edcan.u_stream

import android.content.Context
import android.content.Intent

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
}
