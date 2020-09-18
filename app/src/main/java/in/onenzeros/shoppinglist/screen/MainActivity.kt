package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.adapter.ShoppingListAdapter
import `in`.onenzeros.shoppinglist.enum.UpdateType
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import `in`.onenzeros.shoppinglist.model.DefaultListResponse
import `in`.onenzeros.shoppinglist.model.SuggestionListResponse
import `in`.onenzeros.shoppinglist.model.ShoppingModel
import `in`.onenzeros.shoppinglist.rest.ApiService
import `in`.onenzeros.shoppinglist.rest.request.UpdateListRequest
import `in`.onenzeros.shoppinglist.utils.BaseActivity
import `in`.onenzeros.shoppinglist.utils.PreferenceUtil
import `in`.onenzeros.shoppinglist.utils.Utility
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_add_icon.view.*
import kotlinx.android.synthetic.main.layout_cart_icon.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

//TODO Alka, please avoid _ in package name
class MainActivity : BaseActivity(), BaseActivity.ConnectionChangeListener {

    private val apiService by lazy {
        ApiService.create()
    }

    private lateinit var shoppingAdapter: ShoppingListAdapter
    private lateinit var shoppingResponse: SuggestionListResponse
    private var mShoppingList: ArrayList<ShoppingModel> = arrayListOf()
    private var mCartList: ArrayList<ShoppingModel> = arrayListOf()
    private var mUpdateList: ArrayList<UpdateListRequest> = arrayListOf()
    private var mLocalUpdateList: ArrayList<UpdateListRequest> = arrayListOf()
    private var mSyncUpdateList: ArrayList<UpdateListRequest> = arrayListOf()
    private lateinit var mPreferenceUtil: PreferenceUtil
    private  var id: String = ""

    private lateinit var timer: Timer
    private val noDelay = 0L
    private val everyTenSeconds = 10000L
    private var netConnected = false
    private val suggestionsList : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        initData()
        setConnectionChangeListener(this)
    }

    private fun initData() {
        mPreferenceUtil = PreferenceUtil(this)
        getDefaultSuggestionList()
        mLocalUpdateList = getSavedPendingUpdateList()

        if(netConnected) {
            mPreferenceUtil.getListId()?.let {

                if (it.isNotEmpty()) {
                    id = it
                    existingListAPICall(it)
                } else {
                    defaultListAPICall()
                }
            } ?: kotlin.run {
                defaultListAPICall()
            }
            getSuggestionListAPICall()
            syncPendingList()
            syncFromServer()

        } else {
            val gson = Gson()
            if(!mPreferenceUtil.getPendingList().isNullOrEmpty() && !mPreferenceUtil.getCartList().isNullOrEmpty() ){
                val pendingListString  = mPreferenceUtil.getPendingList()
                val cartListString  = mPreferenceUtil.getCartList()
                val listType = object : TypeToken<List<ShoppingModel>>() {}.type

                id = mPreferenceUtil.getListId().toString()
                mShoppingList  = gson.fromJson(pendingListString, listType)
                mCartList  = gson.fromJson(cartListString, listType)
                mPreferenceUtil.getLastUpdateTime()?.let { setListData(id, it) }
            }
        }

    }

    private fun syncPendingList() {
        getAllPendingUpdateList()
        if(mSyncUpdateList.size>0)
            updateListAPICall(mSyncUpdateList[0],true)
    }

    private fun getAllPendingUpdateList(): ArrayList<UpdateListRequest> {
        mSyncUpdateList.clear()
        if(mLocalUpdateList.size>0) {
            mSyncUpdateList.addAll(mLocalUpdateList)
            mLocalUpdateList.clear()
        }
        if(mUpdateList.size>0) {
            mSyncUpdateList.addAll(mUpdateList)
            mUpdateList.clear()
        }
        return mSyncUpdateList
    }

    private fun getSavedPendingUpdateList(): ArrayList<UpdateListRequest> {
        mPreferenceUtil.getPendingUpdateList()?.let {
            if(it.isNotEmpty()){
                val gson = Gson()
                val listType = object : TypeToken<List<UpdateListRequest>>() {}.type
                val listString  = it
                return gson.fromJson(listString, listType)
            }
        }
        return arrayListOf()
    }

    private fun setSuggestionAdapter() {
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, suggestionsList)
        et_enter_item.setAdapter(adapter)
        et_enter_item.setOnItemClickListener { _, _, _, _ ->
            addToShoppingList()
        }
    }

    private fun getDefaultSuggestionList() {
        val gson = Gson()
        val listItemType = object : TypeToken<SuggestionListResponse>() {}.type

        val shoppingListText: String? = if(mPreferenceUtil.getSuggestionList().isNullOrEmpty()) {
            loadJSONFromAsset()
        } else{
            mPreferenceUtil.getSuggestionList()
        }

        shoppingResponse = gson.fromJson(shoppingListText, listItemType)
        setSuggestionList(shoppingResponse)
    }

    private fun setSuggestionList(shoppingResponse: SuggestionListResponse) {
        suggestionsList.clear()
        shoppingResponse.forEachIndexed { _, shoppingCategory ->
            suggestionsList.addAll(shoppingCategory.items)
        }
        setSuggestionAdapter()
    }

    private fun initUI() {
        shoppingAdapter =
            ShoppingListAdapter(mShoppingList, mCartList)
        shoppingAdapter.setOnItemClickListener(object :
            ShoppingItemClickListener {
            override fun onAddToCart(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                updateListAPICall(UpdateListRequest(id,UpdateType.PICKED.toString(),shoppingModel.name),false)
            }
            override fun onDelete(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                updateListAPICall(UpdateListRequest(id,UpdateType.REMOVE.toString(),shoppingModel.name),false)
            }
            override fun undoToShoppingList(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                updateListAPICall(UpdateListRequest(id,UpdateType.DROPPED.toString(),shoppingModel.name),false)
            }
        })

        rv_shopping.layoutManager = LinearLayoutManager(this)
        rv_shopping.adapter = shoppingAdapter
        rv_shopping.addItemDecoration(
            DividerItemDecoration(
                this,
                (rv_shopping.layoutManager as LinearLayoutManager).orientation
            )
        )

        layout_add_icon.iv_add.setOnClickListener {
            addToShoppingList()
        }

        iv_done.setOnClickListener {
            clearListData()
            defaultListAPICall()
        }

        et_enter_item.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // If the event is a key-down event on the "enter" button
                if (event.action === ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    addToShoppingList()
                    et_enter_item.requestFocus()
                    return true
                }
                return false
            }
        })
    }

    private fun clearListData() {
        mShoppingList.clear()
        mCartList.clear()
        shoppingAdapter.cleaData()
    }

    private fun addToShoppingList() {
        val itemName  = et_enter_item.text.toString()
        if (itemName.isNotEmpty()) {
            if(itemName.trim().isNotEmpty()) {
                groupItemByCategory(itemName)?.let {
                    shoppingAdapter.addShoppingListItem(it)
                    et_enter_item.setText("")
                    updateBadgeCount()
                    updateListAPICall(UpdateListRequest(id, UpdateType.ADD.toString(), itemName),false)
                }
            } else{
                Toast.makeText(this, getString(R.string.valid_shopping_item),Toast.LENGTH_LONG).show()
            }
        }
        else {
            Toast.makeText(this, getString(R.string.empty_shopping_item),Toast.LENGTH_LONG).show()
        }
    }

    private fun groupItemByCategory(itemName: String) : ShoppingModel? {
        var shoppingModel: ShoppingModel? = null
        shoppingResponse.forEachIndexed { _, shoppingCategory ->
            if (shoppingCategory.items.contains(itemName)) {
                shoppingModel =
                    ShoppingModel(
                        shoppingCategory.category,
                        itemName,
                        shoppingCategory.order
                    )
                return shoppingModel
            }
            else
                shoppingModel =
                    ShoppingModel(
                        "Others",
                        itemName,
                        0
                    )
        }
        return shoppingModel
    }

    private fun updateBadgeCount() {
        layout_add_icon.tv_count.text = shoppingAdapter.getShoppingListItemCount().toString()
        tv_cart_count?.text = shoppingAdapter.getCartListItemCount().toString()
    }

    private fun loadJSONFromAsset(): String? {
        val jsonString: String
        try {
            jsonString = assets.open("shoppingItems.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun defaultListAPICall() {
        val call
                = apiService.getDefaultList()

        call.enqueue(object : Callback<DefaultListResponse> {
            override fun onResponse(call: Call<DefaultListResponse>, response: Response<DefaultListResponse>) {
                if (response.code() == 200) {
                    response.body()?.let {
                        parseListData(it)
                    }
                }
            }
            override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
                Log.e("defaultListAPICall","onFailure : ${t.printStackTrace()}")
                Toast.makeText(this@MainActivity, getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getSuggestionListAPICall() {
        val call
                = apiService.getSuggestionList()

        call.enqueue(object : Callback<SuggestionListResponse> {
            override fun onResponse(call: Call<SuggestionListResponse>, response: Response<SuggestionListResponse>) {
                if (response.code() == 200) {
                    response.body()?.let {
                        val listItemType = object : TypeToken<SuggestionListResponse>() {}.type
                        mPreferenceUtil.setSuggestionList(Gson().toJson(it,listItemType))
                        setSuggestionList(it)
                    }
                }
            }
            override fun onFailure(call: Call<SuggestionListResponse>, t: Throwable) {
            }
        })
    }

    private fun existingListAPICall(id: String) {
        val call
                = apiService.getExistingList(id)

        call.enqueue(object : Callback<DefaultListResponse> {
            override fun onResponse(call: Call<DefaultListResponse>, response: Response<DefaultListResponse>) {
                if (response.code() == 200) {
                    response.body()?.let {
                        clearListData()
                        parseListData(it)
                    }
                }
            }
            override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
            }
        })
    }

    private fun updateListAPICall(updateListRequest: UpdateListRequest, isPendingList : Boolean) {
        if(netConnected) {
            val call = apiService.updateExistingList(updateListRequest)

            call.enqueue(object : Callback<DefaultListResponse> {
                override fun onResponse(
                    call: Call<DefaultListResponse>,
                    response: Response<DefaultListResponse>
                ) {
                    if (response.code() == 200) {
                        response.body()?.let {
                            if(isPendingList) {
                                clearListData()
                                parseListData(it)
                                mSyncUpdateList.removeAt(0)
                                savePendingUpdateList(mSyncUpdateList, false)
                                if(mSyncUpdateList.size>0)
                                    updateListAPICall(mSyncUpdateList[0],true)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
                   if(isPendingList)
                       if(mSyncUpdateList.size>0)
                           updateListAPICall(mSyncUpdateList[0],true)
                }
            })
        } else{
            mUpdateList.add(updateListRequest)
        }
    }

    private fun savePendingUpdateList(
        mUpdateList: ArrayList<UpdateListRequest>,
        isNewList: Boolean) {
        var newList = arrayListOf<UpdateListRequest>()

        if(mUpdateList.isNotEmpty()) {
            if (isNewList && mLocalUpdateList.isNotEmpty()) {
                newList.addAll(mLocalUpdateList)
                newList.addAll(mUpdateList)
            } else {
                newList = mUpdateList
            }
        }
            val updateListString = Gson().toJson(newList)
            mPreferenceUtil.setPendingUpdateList(updateListString)
            Log.e("mUpdateList", updateListString)

    }

    private fun parseListData(defaultListResponse: DefaultListResponse) {
        val pendingList = defaultListResponse.pending.distinct()
        pendingList.forEach {
            groupItemByCategory(it)?.let {
                    shoppingModel ->  mShoppingList.add(shoppingModel)
            }
        }
        val cartList = defaultListResponse.cart.distinct()
        cartList.forEach {
            groupItemByCategory(it)?.let {
                    shoppingModel ->  mCartList.add(shoppingModel)
            }
        }
        val time = getString(R.string.last_updated_on_holder,Utility.getDate(defaultListResponse.lastUpdated))
        setListData(defaultListResponse.id,time)
    }

    private fun setListData(id: String, time: String) {
        shoppingAdapter.changeData(mShoppingList,mCartList)
        updateBadgeCount()
        tv_sync_time.visibility = View.VISIBLE
        tv_sync_time.text = time
        saveListToPreference(id, mShoppingList, mCartList,tv_sync_time.text.toString())

        tv_list_id.text = "list id: ${this.id}"
    }

    private fun saveListToPreference(id : String, mShoppingList: ArrayList<ShoppingModel>, mCartList: ArrayList<ShoppingModel>, date: String) {
        this.id = id
        mPreferenceUtil.setListId(id)
        mPreferenceUtil.setPendingList(Gson().toJson(mShoppingList))
        mPreferenceUtil.setCartList(Gson().toJson(mCartList))
        mPreferenceUtil.setLastUpdateTime(date)
    }

    override fun onNetConnectionChanged(isConnected: Boolean) {
        if(isConnected && !netConnected) {
            netConnected = isConnected
            initData()
        } else{
            netConnected = isConnected
        }
    }

    private fun syncFromServer() {
        val timerTask = object : TimerTask() {
            override fun run() {
                if(id.isNotEmpty() && netConnected)
                    existingListAPICall(id)
            }
        }

        timer = Timer()
        timer.schedule(timerTask, noDelay, everyTenSeconds)
    }

    override fun onPause() {
        super.onPause()
        if (::timer.isInitialized) {
            timer.cancel()
            timer.purge()
        }
      }

    override fun onStop() {
        getAllPendingUpdateList()
        savePendingUpdateList(mSyncUpdateList, false)
        saveListToPreference(id, shoppingAdapter.getShoppingList(), shoppingAdapter.getCartList(), tv_sync_time.text.toString())
        super.onStop()
    }

}