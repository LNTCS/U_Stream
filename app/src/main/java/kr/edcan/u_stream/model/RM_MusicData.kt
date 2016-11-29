package kr.edcan.u_stream.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by LNTCS on 2016-03-16.
 */
open class RM_MusicData : RealmObject() {
    @PrimaryKey
    open var id: Int = 0
    open var title: String = ""
    open var videoId: String = ""
    open var playListId: Int = 0
    open var uploader = ""
    open var description: String = ""
    open var thumbnail: String = ""
}
