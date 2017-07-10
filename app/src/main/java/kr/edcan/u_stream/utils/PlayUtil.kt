package kr.edcan.u_stream

import android.content.Context
import android.content.Intent
import com.tramsun.libs.prefcompat.Pref
import com.vicpin.krealmextensions.query
import com.vicpin.krealmextensions.queryFirst
import kr.edcan.u_stream.model.MusicData
import kr.edcan.u_stream.model.RM_MusicData
import java.util.*
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

    fun playOther(context: Context, type: TYPE, musicData: MusicData = MusicData()) {
        when(type){
            TYPE.NEW->{
                if (PlayService.nowPlaying.playListId == musicData.playListId) {
                    setPlayingIndex(musicData)
                } else {
                    setPlayingList(musicData)
                }
                PlayService.nowPlaying = musicData
            }
            TYPE.PREV->{
                PlayService.INDEX--
                if (PlayService.INDEX < 0) {
                    PlayService.INDEX = PlayService.playingList.size - 1
                }
                PlayService.nowPlaying = getMusicByIndex()
            }
            TYPE.NEXT->{
                PlayService.INDEX++
                if (PlayService.INDEX > PlayService.playingList.size - 1) {
                    PlayService.INDEX = 0
                }
                PlayService.nowPlaying = getMusicByIndex()
            }
        }
        startService(context, PlayService.ACTION_START)
    }

    fun setPlayingIndex(musicData: MusicData) {
        if (musicData.id in PlayService.playingList) {
            PlayService.INDEX = PlayService.playingList.indexOf(musicData.id)
        }
    }

    fun setPlayingList(musicData: MusicData) {
        PlayService.playingList.clear()
        RM_MusicData().query { it.equalTo("playListId", musicData.playListId) }.forEach {
            PlayService.playingList.add(it.id)
        }
        if (Pref.getInt("repeatType", 0) == 2) { //셔플상태의 경우 뒤섞기
            Collections.shuffle(PlayService.playingList)
        }
        setPlayingIndex(musicData)
    }

    private fun getMusicByIndex(): MusicData {
        if (PlayService.playingList.size <= 1) {
            return PlayService.nowPlaying
        } else {
            return MusicData(RM_MusicData().queryFirst { it.equalTo("id", PlayService.playingList[PlayService.INDEX]) }!!)
        }
    }

    enum class TYPE{
        NEW, PREV, NEXT
    }
}
