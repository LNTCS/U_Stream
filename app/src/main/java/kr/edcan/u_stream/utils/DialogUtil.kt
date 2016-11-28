package kr.edcan.u_stream.utils

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import kr.edcan.u_stream.R

/**
 * Created by LNTCS on 2016-11-28.
 */
object DialogUtil{

    var mProgressDialog: MaterialDialog? = null

    fun showProgressDialog(mContext: Context, content: String = "로딩중...") {
        if (mProgressDialog == null) {
            mProgressDialog = MaterialDialog.Builder(mContext)
                    .canceledOnTouchOutside(false)
                    .content(content)
                    .widgetColorRes(R.color.colorPrimary)
                    .backgroundColorRes(R.color.colorBg)
                    .progress(true, 0)
                    .build()
        }
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.hide()
        }
    }

    enum class Type {
        POS,
        BOTH
    }

    fun showDialog(mContext: Context, title: String, text: String, type: Type) {
        val builder = MaterialDialog.Builder(mContext)
                .title(title)
                .titleColorRes(R.color.colorPrimary)
                .backgroundColorRes(R.color.colorBgLgt)
                .content(text)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText("확인")
        when (type) {
            DialogUtil.Type.BOTH -> builder.negativeText("취소")
                    .negativeColorRes(R.color.textLgtGray)
        }
        val mDlg = builder.build()
        mDlg.show()
    }
}