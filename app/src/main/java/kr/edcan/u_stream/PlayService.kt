package kr.edcan.u_stream

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.databinding.ObservableArrayList
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.SparseArray
import android.widget.TextView
import at.huber.youtubeExtractor.YouTubeUriExtractor
import at.huber.youtubeExtractor.YtFile
import kr.edcan.u_stream.model.MusicData
import org.jetbrains.anko.toast
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by LNTCS on 2016-11-22.
 */
class PlayService : Service() {

    val NOTIFICATION_NUM = 3939

    companion object {
        val ACTION_START = "kr.edcan.u_stream.action.start"
        val ACTION_RESUME = "kr.edcan.u_stream.action.resume"
        val ACTION_PAUSE = "kr.edcan.u_stream.action.pause"

        var titleViews = ObservableArrayList<TextView>()
        var uploaderViews = ObservableArrayList<TextView>()

        var nowPlaying by Delegates.observable(MusicData()) {
            prop, old, new ->
            var title = new.title
            var uploader = new.uploader

            titleViews.forEach { it.text = title }
            uploaderViews.forEach { it.text = uploader }
        }

        var playingList = ArrayList<Int>()

        fun addTitleView(view: TextView){
            if(view !in titleViews) titleViews.add(view)
            view.text = nowPlaying.title
        }
        fun addUploaderView(view: TextView){
            if(view !in uploaderViews) uploaderViews.add(view)
            view.text = nowPlaying.uploader
        }
    }

    var mediaPlayer: MediaPlayer
    var mContext: Context
    var notification by Delegates.notNull<Notification>()
    var builder by Delegates.notNull<NotificationCompat.Builder>()
    var manager by Delegates.notNull<NotificationManager>()
    var ytEx: YouTubeUriExtractor? = null

    init {
        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        mContext = this
    }

    override fun onCreate() {
        builder = NotificationCompat.Builder(this)
                .setContentText("μ'Stream")
                .setSmallIcon(R.drawable.ic_noti)
        notification = builder.build()
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }
        when(intent.action){
            ACTION_START -> {
                mediaPlayer.reset()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayer.start()
                    startNotification()
                }

                nowPlaying?.let {
                    play()
                }
            }
            ACTION_PAUSE -> {
                stopNotification()
                mediaPlayer.pause()
            }
            ACTION_RESUME -> {
                nowPlaying?.let {
                    startNotification()
                    mediaPlayer.start()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    fun startNotification(){
        builder.setContentTitle("재생중")
        notification = builder.build()
        this.startForeground(NOTIFICATION_NUM, notification)
    }

    fun stopNotification(){
        this.stopForeground(false)
        builder.setContentTitle("멈춰라 얍!")
        notification = builder.build()
        manager.notify(NOTIFICATION_NUM, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun play(){
        ytEx = object : YouTubeUriExtractor(mContext) {
            override fun onUrisAvailable(videoId: String, videoTitle: String, ytFiles: SparseArray<YtFile>?) {
                if (ytFiles != null) {
                    var maxBitrate = 0
                    var link = ""
                    for (i in 0..ytFiles.size() - 1) {
                        val m = ytFiles.get(ytFiles.keyAt(i)).meta
                        if (m.ext.contains("webm") && m.height > 0) {
                            if (maxBitrate < m.audioBitrate) {
                                link = ytFiles.get(ytFiles.keyAt(i)).url
                                maxBitrate = m.audioBitrate
                            }
                        }
                    }
                    mediaPlayer.setDataSource(mContext, Uri.parse(link))
                    mediaPlayer.prepareAsync()
                } else {
                    toast("재생에 문제 발생")
                }
            }
        }
        ytEx!!.execute("https://www.youtube.com/watch?v=${nowPlaying!!.videoId}")
    }

}