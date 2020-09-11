package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.adapter.ShoppingListAdapter
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import `in`.onenzeros.shoppinglist.model.DefaultListResponse
import `in`.onenzeros.shoppinglist.model.ShoppingItemResponse
import `in`.onenzeros.shoppinglist.model.ShoppingModel
import `in`.onenzeros.shoppinglist.rest.ApiService
import `in`.onenzeros.shoppinglist.utils.Utility
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

//TODO Alka, please avoid _ in package name
class MainActivity : AppCompatActivity() {

    private val apiService by lazy {
        ApiService.create()
    }

    private lateinit var shoppingAdapter: ShoppingListAdapter
    private lateinit var shoppingResponse: ShoppingItemResponse
    private var mShoppingList: ArrayList<ShoppingModel> = arrayListOf()
    private var mCartList: ArrayList<ShoppingModel> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        initData()
    }

    private fun initData() {
        defaultListAPICall()

        val gson = Gson()
        val listItemType = object : TypeToken<ShoppingItemResponse>() {}.type
        val suggestionsList : MutableList<String> = mutableListOf()

        shoppingResponse = gson.fromJson(loadJSONFromAsset(), listItemType)
        shoppingResponse.forEachIndexed { _, shoppingCategory ->
            suggestionsList.addAll(shoppingCategory.items)
        }
        Log.e("Main", "${suggestionsList.size}")
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, suggestionsList)
        et_enter_item.setAdapter(adapter)
        et_enter_item.setOnItemClickListener { _, _, _, _ ->
            addToShoppingList()
        }
    }

    private fun initUI() {
        shoppingAdapter =
            ShoppingListAdapter(mShoppingList, mCartList)
        shoppingAdapter.setOnItemClickListener(object :
            ShoppingItemClickListener {
            override fun onAddToCart(pos: Int, name: ShoppingModel) {
                updateBadgeCount()
            }
            override fun onDelete(pos: Int, name: ShoppingModel) {
                updateBadgeCount()
            }
            override fun undoToShoppingList(pos: Int, name: ShoppingModel) {
                updateBadgeCount()
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

    private fun addToShoppingList() {
        val itemName  = et_enter_item.text.toString()

        if (itemName.isNotEmpty()) {
            groupItemByCategory(itemName)?.let {
                shoppingAdapter.addShoppingListItem(it)
                et_enter_item.setText("")
                updateBadgeCount()
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
                        setDefaultList(it)
                    }
                }
            }
            override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
                Log.e("defaultListAPICall","onFailure : ${t.printStackTrace()}")
                Toast.makeText(this@MainActivity, getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setDefaultList(defaultListResponse: DefaultListResponse) {
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
    }

}