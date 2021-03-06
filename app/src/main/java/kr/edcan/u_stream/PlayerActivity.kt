package kr.edcan.u_stream

import android.media.AudioManager
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.tramsun.libs.prefcompat.Pref
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.toolbar_player.*
import kr.edcan.u_stream.PlayUtil.checkMax
import kr.edcan.u_stream.view.SeekArc
import org.jetbrains.anko.audioManager
import org.jetbrains.anko.sdk25.coroutines.onClick

class PlayerActivity : BaseActivity(), View.OnTouchListener, SeekArc.OnSeekArcChangeListener {

    override var viewId: Int = R.layout.activity_player
    override var toolbarId: Int = 0

    var types = intArrayOf(R.drawable.ic_repeat_on, R.drawable.ic_repeat_one_on, R.drawable.ic_shuffle_on)

    override fun onCreate() {
        toolbarTitle.text = "지금 재생중"

        initProgressBar()
        initLayout()
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
        playerRepeatType.onClick {
            Pref.putInt("repeatType", (Pref.getInt("repeatType", 0) + 1) % 3)
            val type = Pref.getInt("repeatType", 0)
            (it as ImageButton).setImageResource(types[type])
            PlayUtil.setPlayingList(PlayService.nowPlaying)
        }
    }

    private fun initVolCtrl() {
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
            setMax(10000)
            setOnTouchListener(this@PlayerActivity)
            setOnSeekArcChangeListener(this@PlayerActivity)
            setSecondaryProgress(0)
            val p = (PlayService.mediaPlayer.currentPosition.toFloat() / 1000).toInt()
            progress = checkMax(p)
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
        playerPlayed.text = PlayUtil.parseTime(checkMax(PlayService.mediaPlayer.currentPosition).toLong())
    }

    override fun onStartTrackingTouch(seekArc: SeekArc) {
    }

    override fun onStopTrackingTouch(seekArc: SeekArc) {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP ->
                playerVolume.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
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
                    val progress = (checkMax(PlayService.mediaPlayer.currentPosition).toFloat() / 1000).toInt()
                    it.progress = progress
                    it.setSecondaryProgress(checkMax(PlayService.buffer))
                }
                if (!PlayService.playable) {
                    it.progress = 0
                    it.setSecondaryProgress(0)
                }
            }
        }

        fun setMaxProgress() {
            var duration = PlayService.mediaPlayer.duration
            checkMax(duration)
            playerSeekBar?.setMax(if (checkMax(duration) != 0) duration / 1000 else 10000)
            PlayService.playingTotal?.text = PlayUtil.parseTime(checkMax(duration).toLong())
        }
    }
}
