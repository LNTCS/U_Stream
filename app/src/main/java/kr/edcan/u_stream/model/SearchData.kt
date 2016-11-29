package kr.edcan.u_stream.model

import android.databinding.BindingAdapter
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import kr.edcan.u_stream.Application.Companion.realm
import kr.edcan.u_stream.utils.DialogUtil

/**
 * Created by LNTCS on 2016-03-15.
 */
enum class SType{
    PLAYLIST, MUSIC
}

class SearchData(val id: String, val title: String, val description: String, val thumbnail: String, val uploader: String, var type: SType) {

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

    val addMusic = View.OnClickListener{
        if (realm.where(RM_PlayListData::class.java).findAll().size == 0) {
            DialogUtil.addPlayListDialog(it.context, this, type)
        } else {
            DialogUtil.selectPlayListDialog(it.context, this, type)
        }
    }
}