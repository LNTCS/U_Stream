package kr.edcan.u_stream

import android.app.Application
import io.realm.Realm
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import kotlin.properties.Delegates

class Application : Application() {

    companion object{
        val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3"
        val KEY_SEARCH = "AIzaSyCGfe8nemQS_9webbrBUODZKtC1PXcpiDM"
        val KEY_LIST = "AIzaSyBidYxusin-5L013M4NDWhmvMojYJMtylc"

        var realm by Delegates.notNull<Realm>()
    }

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NanumBarunGothic.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
        Realm.init(this)
        realm = Realm.getDefaultInstance()
    }
}
