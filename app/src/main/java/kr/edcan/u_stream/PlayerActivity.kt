package kr.edcan.u_stream

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.tramsun.libs.prefcompat.Pref
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.toolbar_player.*
import kr.edcan.u_stream.view.SeekArc
import org.jetbrains.anko.onClick

class PlayerActivity : AppCompatActivity(), View.OnTouchListener, SeekArc.OnSeekArcChangeListener {

    var types = intArrayOf(R.drawable.ic_repeat_on, R.drawable.ic_repeat_one_on, R.drawable.ic_shuffle_on)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        toolbarTitle.text = "지금 재생중"

        initLayout()
        initProgressBar()
        initVolCtrl()
    }

    private fun initLayout() {
        playerSeekBar = playerSeek
        PlayService.playingTotal = playerTotal
        PlayService.mainPlaying = playerControlPlay

        setCurrentProgress()
        primarySeekBarProgressUpdater()
        setMaxProgress()

        PlayService.addTitleView(playerTitle)
        PlayService.addUploaderView(playerSubtitle)
        Glide.with(this).load(R.drawable.bg_default_album).into(playerThumbnail)
        PlayService.playingThumbnail = playerThumbnail
        PlayService.updateView()

        toolbarBack.onClick { onBackPressed() }
        playerRepeatType.setImageResource(types[Pref.getInt("repeatType", 0)])

        playerControlForward.onClick { PlayUtil.playOther(applicationContext, PlayUtil.TYPE.NEXT) }
        playerControlRewind.onClick { PlayUtil.playOther(applicationContext, PlayUtil.TYPE.PREV) }
        playerControlPlay.onClick { PlayService.playORpause() }
    }

    private fun initVolCtrl() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        playerVolume.max = audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC) - 1
        playerVolume.progress = audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC)

        playerVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(arg0: SeekBar) {}

            override fun onStartTrackingTouch(arg0: SeekBar) {}

            override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0)
            }
        })
    }

    private fun initProgressBar() {
        playerSeek.run {
            setMax(100)
            setOnTouchListener(this@PlayerActivity)
            setOnSeekArcChangeListener(this@PlayerActivity)
            setSecondaryProgress(0)
            val p = (PlayService.mediaPlayer.currentPosition.toFloat() / 1000).toInt()
            progress = if (p > 0) p else 0
            setSecondaryProgress(PlayService.buffer)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (v.id == R.id.playerSeek) {
                val sb = v as SeekArc
                val playPositionInMillisecconds = sb.progress * 1000
                PlayService.mediaPlayer.seekTo(playPositionInMillisecconds)
            }
        }
        return false
    }

    override fun onProgressChanged(seekArc: SeekArc, progress: Int, fromUser: Boolean) {
        playerPlayed.text = PlayUtil.parseTime(PlayService.mediaPlayer.currentPosition.toLong())
    }

    override fun onStartTrackingTouch(seekArc: SeekArc) {
    }

    override fun onStopTrackingTouch(seekArc: SeekArc) {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> playerVolume.progress = playerVolume.progress - 1
            KeyEvent.KEYCODE_VOLUME_UP -> playerVolume.progress = playerVolume.progress + 1
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        var playerSeekBar: SeekArc? = null
        @JvmStatic
        fun primarySeekBarProgressUpdater() {
            setCurrentProgress()
            val notification = Runnable { primarySeekBarProgressUpdater() }
            Handler().postDelayed(notification, 1000)
        }

        fun setCurrentProgress() {
            playerSeekBar?.let {
                if (!it.isTouching) {
                    val progress = (PlayService.mediaPlayer.currentPosition.toFloat() / 1000).toInt()
                    it.progress = if (progress > 0) progress else 0
                    it.setSecondaryProgress(PlayService.buffer)
                }
                if (!PlayService.playable) {
                    it.progress = 0
                    it.setSecondaryProgress(0)
                }
            }
        }

        fun setMaxProgress() {
            playerSeekBar?.setMax(PlayService.mediaPlayer.duration / 1000)
            PlayService.playingTotal?.text = PlayUtil.parseTime(PlayService.mediaPlayer.duration.toLong())
        }
    }
}
//todo 재생중에 플레이 들가면 max 미설정
