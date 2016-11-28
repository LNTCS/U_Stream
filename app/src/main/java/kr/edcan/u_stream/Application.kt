package kr.edcan.u_stream

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NanumBarunGothic.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
}
