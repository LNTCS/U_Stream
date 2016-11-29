package kr.edcan.u_stream.utils

import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.kittinunf.fuel.httpGet
import kr.edcan.u_stream.Application.Companion.KEY_LIST
import kr.edcan.u_stream.Application.Companion.YOUTUBE_BASE_URL
import kr.edcan.u_stream.Application.Companion.realm
import kr.edcan.u_stream.R
import kr.edcan.u_stream.adpater.PlayListSpinnerAdapter
import kr.edcan.u_stream.model.RM_MusicData
import kr.edcan.u_stream.model.RM_PlayListData
import kr.edcan.u_stream.model.SType
import kr.edcan.u_stream.model.SearchData
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.util.*

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

    fun addPlayListDialog(mContext: Context, data: SearchData, type: SType) {
        val mDlg = MaterialDialog.Builder(mContext).run {
            title("재생목록 추가")
            titleColorRes(R.color.colorPrimary)
            content("'" + data.title + "'을(를) 새로운 재생목록에 추가합니다.")
            backgroundColorRes(R.color.colorBgLgt  )
            positiveColorRes(R.color.colorPrimary)
            positiveText("추가")
            negativeColorRes(R.color.textGray)
            negativeText("취소")
            inputType(InputType.TYPE_CLASS_TEXT)
            input("제목을 입력해주세요.", null) { dialog, input -> }
            widgetColorRes(R.color.colorPrimary)
            onPositive { dialog, which ->
                val input = dialog.inputEditText!!.text.toString()
                if (input.trim() == "") {
                    dialog.dismiss()
                    addPlayListDialog(mContext, data, type)
                } else {
                    if (type == SType.MUSIC) {
                        val listId = getNumberInt(realm.where(RM_PlayListData::class.java).max("id")) + 1
                        val musicId = getNumberInt(realm.where(RM_MusicData::class.java).max("id")) + 1

                        val pData = RM_PlayListData()
                        pData.id = listId
                        pData.title = input

                        val mData = RM_MusicData()
                        mData.run {
                            id = musicId
                            title = data.title
                            playListId = listId
                            thumbnail = data.thumbnail
                            uploader = data.uploader
                            description = data.description
                            videoId = data.id
                        }
                        realm.executeTransaction({ realm ->
                            realm.copyToRealm(pData)
                            realm.copyToRealm(mData)
                        })
                        mContext.toast(input + "에 1곡이 추가되었습니다.")
                    } else {
                        val listId = getNumberInt(realm.where(RM_PlayListData::class.java).max("id")) + 1
                        val musicId = getNumberInt(realm.where(RM_MusicData::class.java).max("id")) + 1
                        val pData = RM_PlayListData()
                        pData.id = listId
                        pData.title = input
                        realm.executeTransaction({ realm -> realm.copyToRealm(pData) })
                        getMusics(mContext, data.id, pData.id, musicId, pData.title)
                    }
                }
            }
        }.build()
        mDlg.show()
    }

    fun selectPlayListDialog(mContext: Context, data: SearchData, type: SType) {
        var selectListPos = 0
        val wrapInScrollView = true
        var addDlg = MaterialDialog.Builder(mContext).run {
                title("재생목록에 추가")
                titleColorRes(R.color.colorPrimary)
                customView(R.layout.content_dialog_add_playlist, wrapInScrollView)
                backgroundColorRes(R.color.colorBgLgt)
                positiveColorRes(R.color.colorPrimary)
                positiveText("확인")
                negativeColorRes(R.color.textGray)
                negativeText("취소")
        }.build()
        val view = addDlg.customView!!

        view.find<TextView>(R.id.addDlgContent).text = "'${data.title}'을(를) 재생목록에 추가합니다."

        val spinnerAdapter = PlayListSpinnerAdapter(mContext)

        val pList = realm.where(RM_PlayListData::class.java).findAll()
        for (pData in pList) {
            spinnerAdapter.addItem(pData.title)
        }
        spinnerAdapter.addItem("새 재생목록 추가...")
        view.find<Spinner>(R.id.addDlgSpinner).run {
            adapter = spinnerAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if (parent.count - 1 == position) {
                        selectListPos = -1
                    } else {
                        selectListPos = position
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {  }
            }
        }
        addDlg.builder.onPositive(MaterialDialog.SingleButtonCallback { dialog, which ->
            if (selectListPos == -1) {
                addPlayListDialog(mContext, data, type)
                return@SingleButtonCallback
            }
            val pData = pList[selectListPos]
            val musicId = getNumberInt(realm.where(RM_MusicData::class.java).max("id")) + 1

            if (type == SType.MUSIC) {
                val mData = RM_MusicData()
                mData.id = musicId
                mData.title = data.title
                mData.playListId = pData.id
                mData.thumbnail = data.thumbnail
                mData.uploader = data.uploader
                mData.description = data.description
                mData.videoId = data.id
                realm.executeTransaction { realm -> realm.copyToRealm(mData) }
                Toast.makeText(mContext, pData.title + "에 1곡이 추가되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                getMusics(mContext, data.id, pData.id, musicId, pData.title)
            }
        }).build().show()
    }

    fun getMusics(mContext: Context, id: String, playlistId: Int, musicId: Int, title: String) {
        showProgressDialog(mContext, content = "재생목록 분석중...")
        val mList = ArrayList<RM_MusicData>()
        var musicId = musicId

        (YOUTUBE_BASE_URL + "/playlistItems").httpGet(
                listOf("part" to "snippet", "maxResults" to 50, "key" to KEY_LIST, "playlistId" to id)
        ).responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val jsonObject = JSONObject(String(response.data))
                val items = jsonObject.getJSONArray("items")
                for (i in 0..items.length() - 1) {
                    val snippet = items.getJSONObject(i).getJSONObject("snippet")
                    val mData = RM_MusicData()
                    mData.id = musicId
                    mData.title = snippet.getString("title")
                    if (mData.title == "Deleted video" || mData.title == "") {
                        continue
                    }
                    mData.playListId = playlistId
                    if (snippet.has("thumbnails") && snippet.getJSONObject("thumbnails").has("medium")) {
                        mData.thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url")
                    } else {
                        mData.thumbnail = ""
                    }
                    mData.uploader = snippet.getString("channelTitle")
                    mData.description = snippet.getString("description")
                    mData.videoId = snippet.getJSONObject("resourceId").getString("videoId")
                    mList.add(mData)
                    musicId++
                }
                if (jsonObject.has("nextPageToken")) {
                    getNext(mContext, jsonObject.getString("nextPageToken"), mList, id, playlistId, musicId)
                }
                realm.executeTransaction { realm ->
                    for (mData in mList) {
                        realm.copyToRealm<RM_MusicData>(mData)
                    }
                }
                hideProgressDialog()
                mContext.toast(title + "에 " + mList.size + "곡이 추가되었습니다.")
            }else{
                hideProgressDialog()
                failDialog(mContext, title = "분석 실패", content = "분석 중 오류가 발생하였습니다.\n다시 시도해 주세요.")
            }
        }
    }

    fun getNext(mContext: Context, nextPageToken: String, mList: ArrayList<RM_MusicData>, id: String, playlistId: Int, musicId: Int) {
        var musicId = musicId
        (YOUTUBE_BASE_URL + "/playlistItems").httpGet(
                listOf("part" to "snippet", "maxResults" to 50, "key" to KEY_LIST, "playlistId" to id, "pageToken" to nextPageToken)
        ).responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val jsonObject = JSONObject(String(response.data))
                val items = jsonObject.getJSONArray("items")
                for (i in 0..items.length() - 1) {
                    val snippet = items.getJSONObject(i).getJSONObject("snippet")
                    val mData = RM_MusicData()
                    mData.id = musicId
                    mData.title = snippet.getString("title")
                    if (mData.title == "Deleted video" || mData.title == "") {
                        continue
                    }
                    mData.playListId = playlistId
                    if (snippet.has("thumbnails") && snippet.getJSONObject("thumbnails").has("medium")) {
                        mData.thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url")
                    } else {
                        mData.thumbnail = ""
                    }
                    mData.uploader = snippet.getString("channelTitle")
                    mData.description = snippet.getString("description")
                    mData.videoId = snippet.getJSONObject("resourceId").getString("videoId")
                    mList.add(mData)
                    musicId++
                }
                if (jsonObject.has("nextPageToken")) {
                    getNext(mContext, jsonObject.getString("nextPageToken"), mList, id, playlistId, musicId)
                }
            }else{
                hideProgressDialog()
                failDialog(mContext, title = "분석 실패", content = "분석 중 오류가 발생하였습니다.\n다시 시도해 주세요.")
            }
        }
    }

    private fun getNumberInt(num: Number?) = num?.toInt() ?: 0

    fun failDialog(mContext: Context, title: String = "", content: String = ""){
        DialogUtil.showDialog(mContext, title, content, DialogUtil.Type.POS)
    }
}