package kr.edcan.u_stream

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.toolbar_search.*
import kr.edcan.u_stream.adpater.SearchResultPagerAdapter
import kr.edcan.u_stream.model.SearchData
import kr.edcan.u_stream.utils.DialogUtil
import org.jetbrains.anko.onClick
import org.json.JSONException
import org.json.JSONObject
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.*
import kotlin.properties.Delegates

class SearchActivity : AppCompatActivity() {

    var searchMusics = ArrayList<SearchData>()
    var searchLists = ArrayList<SearchData>()
    var searchResultAdapter by Delegates.notNull<SearchResultPagerAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        FuelManager.instance.basePath = Application.YOUTUBE_BASE_URL

        searchEdit.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                return@OnEditorActionListener true
            }
            false
        })

        searchResultAdapter = SearchResultPagerAdapter(this, searchMusics, searchLists)
        searchPager.adapter = searchResultAdapter
        searchTab.setViewPager(searchPager)

        searchBtn.onClick { performSearch() }
    }

    internal fun performSearch() {
        searchMusics.clear()
        searchLists.clear()
        DialogUtil.showProgressDialog(this)
        "/search".httpGet(
                listOf("part" to "snippet", "key" to Application.KEY_SEARCH,
                        "q" to searchEdit.text.toString(), "maxResults" to 50, "type" to "video")
        ).responseString { request, response, result ->
            val (data, error) = result
            if (error == null) {
                val musicItems = JSONObject(String(response.data)).getJSONArray("items")
                for (i in 0..musicItems.length() - 1) {
                    val musicItem = musicItems.getJSONObject(i)
                    val snippet = musicItem.getJSONObject("snippet")
                    val data = SearchData(
                            musicItem.getJSONObject("id").optString("videoId", ""),
                            snippet.optString("title", ""),
                            snippet.optString("description", ""),
                            getThumb(snippet),
                            snippet.optString("channelTitle", "")
                    )
                    searchMusics.add(data)
                    searchResultAdapter.notifyDataSetChanged()
                }
                // 재생목록 추가 로딩
                "/search".httpGet(
                        listOf("part" to "snippet", "key" to Application.KEY_LIST,
                                "q" to searchEdit.text.toString(), "maxResults" to 50, "type" to "playlist")
                ).responseString { request, response, result ->
                    val (data, error) = result
                    if (error == null) {
                        val musicItems = JSONObject(String(response.data)).getJSONArray("items")
                        for (i in 0..musicItems.length() - 1) {
                            val musicItem = musicItems.getJSONObject(i)
                            val snippet = musicItem.getJSONObject("snippet")
                            val data = SearchData(
                                    musicItem.getJSONObject("id").optString("videoId", ""),
                                    snippet.optString("title", ""),
                                    snippet.optString("description", ""),
                                    getThumb(snippet),
                                    snippet.optString("channelTitle", "")
                            )
                            searchLists.add(data)
                            searchResultAdapter.notifyDataSetChanged()
                        }
                        DialogUtil.hideProgressDialog()
                    } else { //재생목록 로딩 실패
                        DialogUtil.hideProgressDialog()
                        failDialog()
                    }
                }
            } else { //영상 로딩 실패
                DialogUtil.hideProgressDialog()
                failDialog()
            }
        }
    }

    fun failDialog(){
        DialogUtil.showDialog(this, "검색 실패", "검색 중 오류가 발생하였습니다.\n다시 시도해 주세요.", DialogUtil.Type.POS)
    }

    @Throws(JSONException::class)
    private fun getThumb(snippet: JSONObject): String {
        var result = ""
        if (!snippet.has("thumbnails"))
            return result
        val jsonObject = snippet.getJSONObject("thumbnails")
        if (jsonObject.has("high") && !jsonObject.isNull("high")) {
            result = jsonObject.getJSONObject("high").optString("url", "")
        } else if (jsonObject.has("medium") && !jsonObject.isNull("medium")) {
            result = jsonObject.getJSONObject("medium").optString("url", "")
        } else if (jsonObject.has("default") && !jsonObject.isNull("high")) {
            result = jsonObject.getJSONObject("default").optString("url", "")
        }
        return result
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base))
    }
}
