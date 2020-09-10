package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.adapter.ShoppingListAdapter
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import `in`.onenzeros.shoppinglist.model.DefaultListResponse
import `in`.onenzeros.shoppinglist.model.ShoppingItemResponse
import `in`.onenzeros.shoppinglist.rest.ApiService
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
import java.io.InputStream

//TODO Alka, please avoid _ in package name
class MainActivity : AppCompatActivity() {

    private val apiService by lazy {
        ApiService.create()
    }

    private lateinit var shoppingAdapter: ShoppingListAdapter
    private var mShoppingList: MutableList<String> = arrayListOf()
    private var mCartList: MutableList<String> = arrayListOf()

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

        var shoppingResponse: ShoppingItemResponse = gson.fromJson(loadJSONFromAsset(), listItemType)
        shoppingResponse.forEachIndexed { idx, shoppingCategory ->
            suggestionsList.addAll(shoppingCategory.items)
        }
        Log.e("Main", "${suggestionsList.size}")
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, suggestionsList)
        et_enter_item.setAdapter(adapter)
    }

    private fun initUI() {
        shoppingAdapter =
            ShoppingListAdapter(mShoppingList, mCartList)
        shoppingAdapter.setOnItemClickListener(object :
            ShoppingItemClickListener {
            override fun onAddToCart(pos: Int, name: String) {
                updateBadgeCount()
            }
            override fun onDelete(pos: Int, name: String) {
                updateBadgeCount()
            }
            override fun undoToShoppingList(pos: Int, name: String) {
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
        if (et_enter_item.text.toString().isNotEmpty()) {
            shoppingAdapter.addShoppingListItem(et_enter_item.text.toString())
            et_enter_item.setText("")
            updateBadgeCount()
        }
        else {
            Toast.makeText(this, getString(R.string.empty_shopping_item),Toast.LENGTH_LONG).show()
        }
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
        mShoppingList = defaultListResponse.pending as MutableList<String>
        mCartList = defaultListResponse.cart as MutableList<String>
        shoppingAdapter.changeData(mShoppingList,mCartList)
        updateBadgeCount()
    }

}