package kr.edcan.u_stream

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableArrayList
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.SparseArray
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.NotificationTarget
import com.google.gson.Gson
import com.tramsun.libs.prefcompat.Pref
import kr.edcan.u_stream.model.MusicData
import org.jetbrains.anko.toast
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by LNTCS on 2016-11-22.
 */
class PlayService : Service() {


    companion object {
        val NOTIFICATION_NUM = 3939
        val ACTION_INIT = "kr.edcan.u_stream.action.init"
        val ACTION_START = "kr.edcan.u_stream.action.start"
        val ACTION_RESUME = "kr.edcan.u_stream.action.resume"
        val ACTION_PAUSE = "kr.edcan.u_stream.action.pause"

        var titleViews = ObservableArrayList<TextView>()
        var uploaderViews = ObservableArrayList<TextView>()
        var btmPlaying: ImageView? = null
        var playingList = ArrayList<Int>()
        var isInitial = false
        var mediaPlayer: MediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnBufferingUpdateListener { mp, per ->
                var percent = per
                if (percent == 0) percent = 1
                buffer = percent
            }
            setOnPreparedListener {
                playable = true
                updateView()
//                updateTimePrg()
//                updateState(Pair(nowPlaying.title, nowPlaying.uploader))
            }
            setOnCompletionListener {
//                updateState(Pair(nowPlaying.title, nowPlaying.uploader))
//                PlayUtil.playOther(mContext, true) // 한곡재생이라면 여기서 다시 프로그레스를 0으로
            }
        }

        var buffer = 0

        var nowPlaying by Delegates.observable(MusicData()) {
            prop, old, new ->
            var title = new.title
            var uploader = new.uploader

            titleViews.forEach { it.text = title }
            uploaderViews.forEach { it.text = uploader }
            Pref.putString("latestPlay", Gson().toJson(new))
        }

        fun addTitleView(view: TextView) {
            if (view !in titleViews) titleViews.add(view)
            view.text = nowPlaying.title
        }

        fun addUploaderView(view: TextView) {
            if (view !in uploaderViews) uploaderViews.add(view)
            view.text = nowPlaying.uploader
        }

        fun playORpause() {
            if (isInitial) {
                PlayUtil.startService(mContext, PlayService.ACTION_START)
                isInitial = false
            } else if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                PlayUtil.startService(mContext, PlayService.ACTION_PAUSE)
            } else {
                mediaPlayer.start()
                PlayUtil.startService(mContext, PlayService.ACTION_RESUME)
            }
            updateView()
        }

        fun updateView() {
            notification?.let {
                remoteView?.let {
                    it.setTextViewText(R.id.notifyTitle, nowPlaying.title)
                    it.setTextViewText(R.id.notifySubtitle, nowPlaying.uploader)
                    it.setImageViewResource(R.id.notifyPlay, if (mediaPlayer.isPlaying) R.drawable.selector_notify_pause else R.drawable.selector_notify_play)
                    val notificationTarget = NotificationTarget(mContext, it, R.id.notifyThumb, notification, NOTIFICATION_NUM)
                    Glide.with(mContext).load(nowPlaying.thumbUri).asBitmap().placeholder(R.drawable.ic_notify_album).into(notificationTarget)
                }
            }
            btmPlaying?.setImageResource(if (mediaPlayer.isPlaying) R.drawable.ic_btm_pause else R.drawable.ic_btm_play)
        }

        var beforeEvent: Long = 0
        var playable = true
        var mContext by Delegates.notNull<Context>()
        var notification: Notification? = null
        var remoteView: RemoteViews? = null
        var builder by Delegates.notNull<NotificationCompat.Builder>()
        var manager by Delegates.notNull<NotificationManager>()
        var ytEx: YouTubeExtractor? = null
    }

    init {
        mContext = this
    }

    override fun onCreate() {
        remoteView = RemoteViews(mContext.packageName, R.layout.content_notification)
        remoteView!!.setImageViewResource(R.id.notifyPlay, if (mediaPlayer.isPlaying) R.drawable.selector_notify_pause else R.drawable.selector_notify_play)

        builder = NotificationCompat.Builder(this)
                .setContentText("μ'Stream")
                .setSmallIcon(R.drawable.ic_noti)
                .setCustomContentView(remoteView)

        notification = builder.build()
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setIntent(remoteView!!)
        PlayerActivity.primarySeekBarProgressUpdater(PlayerActivity.playerSeekBar)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }
        when (intent.action) {
            ACTION_INIT -> {
                isInitial = true
                stopNotification()
            }
            ACTION_START -> {
                mediaPlayer.reset()
                mediaPlayer.setOnPreparedListener {
                    playable = true
                    mediaPlayer.start()
                    startNotification()
                }
                play()
            }
            ACTION_PAUSE -> {
                stopNotification()
                mediaPlayer.pause()
            }
            ACTION_RESUME -> {
                startNotification()
                mediaPlayer.start()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    fun startNotification() {
        notification = builder.build()
        updateView()
        this.startForeground(NOTIFICATION_NUM, notification)
    }

    fun stopNotification() {
        this.stopForeground(false)
        notification = builder.build()
        updateView()
        manager.notify(NOTIFICATION_NUM, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun play() {
        playable = false
        ytEx = object : YouTubeExtractor(mContext) {
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, meta: VideoMeta) = if (ytFiles != null) {
                var maxBitrate = 0
                var link = ""
                for (i in 0..ytFiles.size() - 1) {
                    val m = ytFiles.get(ytFiles.keyAt(i)).format
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
                //todo play next music
                toast("재생에 문제 발생")
//                PlayUtil.playOther(mContext, true)
            }
        }
        ytEx!!.extract("https://www.youtube.com/watch?v=${nowPlaying.videoId}", true, true)
    }

    fun setIntent(views: RemoteViews) {
        val playIntent = Intent("kr.edcan.ustream.control.play")
        val pendingIntent = PendingIntent.getBroadcast(mContext, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(R.id.notifyPlay, pendingIntent)
        val intentFilter = IntentFilter()
        intentFilter.addAction("kr.edcan.ustream.control.play")
        mContext.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val currentEvent = System.currentTimeMillis()
                if (beforeEvent < currentEvent - 100 && playable) {
                    playORpause()
                }
                beforeEvent = currentEvent
            }
        }, intentFilter)

        val forwardIntent = Intent("kr.edcan.ustream.control.forward")
        val pendingIntentF = PendingIntent.getBroadcast(mContext, 0, forwardIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(R.id.notifyNext, pendingIntentF)
        val intentFilterF = IntentFilter()
        intentFilterF.addAction("kr.edcan.ustream.control.forward")
        mContext.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val currentEvent = System.currentTimeMillis()
                if (beforeEvent < currentEvent - 100 && playable) {
                    // TODO 다음곡 재생
                }
                beforeEvent = currentEvent
            }
        }, intentFilterF)
    }
}
