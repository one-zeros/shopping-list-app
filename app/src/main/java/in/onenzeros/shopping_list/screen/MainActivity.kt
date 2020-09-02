package `in`.onenzeros.shopping_list.screen

import `in`.onenzeros.shopping_list.R
import `in`.onenzeros.shopping_list.adapter.CartAdapter
import `in`.onenzeros.shopping_list.adapter.ShoppingAdapter
import `in`.onenzeros.shopping_list.listener.ShoppingItemClickListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var shoppingAdapter : ShoppingAdapter
    lateinit var cartAdapter: CartAdapter
    var shoppingList = arrayListOf<String>()
    var cartList = arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI() {
        cartAdapter = CartAdapter(cartList)
        rv_cart.layoutManager = LinearLayoutManager(this)
        rv_cart.adapter = cartAdapter

        shoppingAdapter = ShoppingAdapter(shoppingList)
        shoppingAdapter.setOnItemClickListener(object : ShoppingItemClickListener{
            override fun onAddToCart(pos: Int, name: String) {
            }

            override fun onDelete(pos: Int, name: String) {
            }

        })
        rv_shopping.layoutManager = LinearLayoutManager(this)
        rv_shopping.adapter = shoppingAdapter

        iv_add.setOnClickListener {
            shoppingList.add(et_enter_item.text.toString())
            shoppingAdapter.addData(et_enter_item.text.toString())
        }

    }
}