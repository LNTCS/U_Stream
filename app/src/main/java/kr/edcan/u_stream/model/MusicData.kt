package kr.edcan.u_stream.model

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide



/**
 * Created by LNTCS on 2016-03-15.
 */

class MusicData{
    var id = 0
    var title = ""
    var videoId = ""
    var playListId = 0
    var uploader = ""
    var description = ""
    var thumbUri = ""

    constructor(id: Int,  title: String,  videoId: String,  playListId: Int,  uploader: String,  description: String,  thumbUri: String){
        this.id = id
        this.title = title
        this.videoId = videoId
        this.playListId = playListId
        this.uploader = uploader
        this.description = description
        this.thumbUri = thumbUri
    }

    constructor(sData: SearchData){
        this.title = sData.title
        this.videoId = sData.id
        this.uploader = sData.uploader
        this.description = sData.description
        this.thumbUri = sData.thumbnail
    }

    constructor(){
        this.title = "μ'Stream"
        this.uploader = "by EDCAN"
    }

    constructor(mData: RM_MusicData){
        this.id = mData.id
        this.title = mData.title
        this.videoId = mData.videoId
        this.playListId = mData.playListId
        this.uploader = mData.uploader
        this.description = mData.description
        this.thumbUri = mData.thumbnail
    }

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