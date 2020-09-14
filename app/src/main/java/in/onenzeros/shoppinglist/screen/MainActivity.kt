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
    private lateinit var mPreferenceUtil: PreferenceUtil
    private  var id: String = ""

    private lateinit var timer: Timer
    private val noDelay = 0L
    private val everyTenSeconds = 10000L
    private var netConnected = false
    val suggestionsList : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        initData()
        setConnectionChangeListener(this)
    }

    private fun initData() {
        mPreferenceUtil = PreferenceUtil(this)
        mPreferenceUtil.getListId()?.let {

            if (it.isNotEmpty()) {
                id = it
                existingListAPICall(it)
            }
            else {
                defaultListAPICall()
            }
        } ?: kotlin.run {
            defaultListAPICall()
        }

        getDefaultSuggestionList()
        getSuggestionListAPICall()
    }

    private fun setSuggestionAdapter() {
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, suggestionsList)
        et_enter_item.setAdapter(adapter)
        et_enter_item.setOnItemClickListener { _, _, _, _ ->
            addToShoppingList()
        }    }

    private fun getDefaultSuggestionList() {
        var shoppingListText : String? = ""
        val gson = Gson()
        val listItemType = object : TypeToken<SuggestionListResponse>() {}.type

        Log.e("loadJSONFromAsset",loadJSONFromAsset())

        shoppingListText = if(mPreferenceUtil.getSuggestionList().isNullOrEmpty()) {
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
        Log.e("Main", "${suggestionsList.size}")
        setSuggestionAdapter()
    }

    private fun initUI() {
        shoppingAdapter =
            ShoppingListAdapter(mShoppingList, mCartList)
        shoppingAdapter.setOnItemClickListener(object :
            ShoppingItemClickListener {
            override fun onAddToCart(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                updateListAPICall(id,UpdateType.PICKED.toString(),shoppingModel.name)
            }
            override fun onDelete(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                updateListAPICall(id,UpdateType.REMOVE.toString(),shoppingModel.name)
            }
            override fun undoToShoppingList(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                updateListAPICall(id,UpdateType.DROPPED.toString(),shoppingModel.name)
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

    override fun onResume() {
        super.onResume()
        syncFromServer()
    }

    private fun addToShoppingList() {
        val itemName  = et_enter_item.text.toString()
        if (itemName.isNotEmpty()) {
            if(itemName.trim().isNotEmpty()) {
                groupItemByCategory(itemName)?.let {
                    shoppingAdapter.addShoppingListItem(it)
                    et_enter_item.setText("")
                    updateBadgeCount()
                    updateListAPICall(id, UpdateType.ADD.toString(), itemName)
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
        tv_cart_count.text = shoppingAdapter.getCartListItemCount().toString()
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
                        setListData(it)
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
                        setListData(it)
                    }
                }
            }
            override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
                Log.e("defaultListAPICall","onFailure : ${t.printStackTrace()}")
                Toast.makeText(this@MainActivity, getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateListAPICall(id: String,action :String, item : String) {
        val call
                = apiService.updateExistingList(UpdateListRequest(id, action, item))

        call.enqueue(object : Callback<DefaultListResponse> {
            override fun onResponse(call: Call<DefaultListResponse>, response: Response<DefaultListResponse>) {
                if (response.code() == 200) {
                    response.body()?.let {
//                        setListData(it)
                    }
                }
            }
            override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
                Log.e("defaultListAPICall","onFailure : ${t.printStackTrace()}")
                Toast.makeText(this@MainActivity, getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setListData(defaultListResponse: DefaultListResponse) {
        id = defaultListResponse.id
        mPreferenceUtil.setListId(id)
        defaultListResponse.pending.forEach {
            groupItemByCategory(it)?.let {
                    shoppingModel ->  mShoppingList.add(shoppingModel)
            }
        }
        defaultListResponse.cart.forEach {
            groupItemByCategory(it)?.let {
                    shoppingModel ->  mCartList.add(shoppingModel)
            }
        }
        shoppingAdapter.changeData(mShoppingList,mCartList)
        updateBadgeCount()
        tv_sync_time.visibility = View.VISIBLE
        tv_sync_time.text = getString(R.string.last_updated_on_holder,Utility.getDate(defaultListResponse.lastUpdated))
        tv_list_id.text = "list id: $id"
       Log.e("shopping list", "list id: ${id}")
    }

    override fun onNetConnectionChanged(isConnected: Boolean) {
        if(isConnected) {
            netConnected = isConnected
            initData()
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

    override fun onStop() {
        super.onStop()
        timer.cancel()
        timer.purge()
    }

}