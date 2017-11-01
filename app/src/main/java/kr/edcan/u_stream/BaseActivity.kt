package kr.edcan.u_stream

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem

/**
 * Created by LNTCS on 2017-09-26.
 */

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewId)
        if (toolbarId != 0) {
            var toolbar = findViewById(toolbarId) as Toolbar
            setSupportActionBar(toolbar)
        }
        onCreate()
    }

    protected abstract var viewId: Int
    protected abstract var toolbarId: Int

    protected abstract fun onCreate()

    fun disableToggle() {
        this.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    fun enableToggle() {
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun setToolbarTitle(titleStr: String) {
        this.supportActionBar?.title = titleStr
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
