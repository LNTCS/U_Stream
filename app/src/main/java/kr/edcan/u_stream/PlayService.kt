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
import android.widget.RemoteViews
import android.widget.TextView
import at.huber.youtubeExtractor.YouTubeUriExtractor
import at.huber.youtubeExtractor.YtFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.NotificationTarget
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

    var beforeEvent: Long = 0
    var playable = true
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
        val view = RemoteViews(mContext.packageName, R.layout.content_notification)
        view.setImageViewResource(R.id.notifyPlay, if (mediaPlayer.isPlaying) R.drawable.selector_notify_pause else R.drawable.selector_notify_play)

        builder = NotificationCompat.Builder(this)
                .setContentText("μ'Stream")
                .setSmallIcon(R.drawable.ic_noti)
                .setCustomContentView(view)

        notification = builder.build()
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val i = Intent(mContext, PlayerActivity::class.java)
//        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//        val pi = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
//        notification.contentIntent = pi
        setIntent(view)
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
                    playable = true
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
        updateNotification()
        this.startForeground(NOTIFICATION_NUM, notification)
    }

    fun stopNotification(){
        this.stopForeground(false)
        builder.setContentTitle("멈춰라 얍!")
        notification = builder.build()
        updateNotification()
        manager.notify(NOTIFICATION_NUM, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun play(){
        playable = false
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

    fun updateNotification(){
        val rv = notification.contentView
        rv?.let{
            rv.setTextViewText(R.id.notifyTitle, nowPlaying.title)
            rv.setTextViewText(R.id.notifySubtitle, nowPlaying.uploader)
            rv.setImageViewResource(R.id.notifyPlay, if (mediaPlayer.isPlaying) R.drawable.selector_notify_pause else R.drawable.selector_notify_play)
            val notificationTarget = NotificationTarget(mContext, rv, R.id.notifyThumb, notification, NOTIFICATION_NUM)
            Glide.with(mContext).load(nowPlaying.thumbUri).asBitmap().placeholder(R.drawable.ic_notify_album).into(notificationTarget)
        }
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
                    if (mediaPlayer.isPlaying)
                        mediaPlayer.pause()
                    else
                        mediaPlayer.start()
                }
                beforeEvent = currentEvent
                updateNotification()
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
