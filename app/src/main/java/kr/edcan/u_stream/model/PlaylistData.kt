package kr.edcan.u_stream.model

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Created by LNTCS on 2016-03-15.
 */

class PlaylistData(val id: Int, val title: String, val count: Int, val thumbUri: String) {

    object ImageViewBindingAdapter {
        @BindingAdapter("imageUrl")
        @JvmStatic
        fun loadImage(view: ImageView?, url: String?) {
            Glide.with(view!!.context)
                    .load(url)
                    .skipMemoryCache(true)
                    .into(view)
        }
    }
    fun getCountText() = "${count}곡이 플레이 리스트에 있음"
}