package kr.edcan.u_stream.model

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Created by LNTCS on 2016-03-15.
 */
class SearchData(val id: String, val title: String, val description: String, val thumbnail: String, val uploader: String) {

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
}