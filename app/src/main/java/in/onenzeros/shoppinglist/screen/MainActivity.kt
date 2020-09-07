package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.adapter.CartAdapter
import `in`.onenzeros.shoppinglist.adapter.ShoppingAdapter
import `in`.onenzeros.shoppinglist.listener.CartItemClickListener
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_add_icon.view.*
import kotlinx.android.synthetic.main.layout_cart_icon.*

//TODO Alka, please avoid _ in package name
class MainActivity : AppCompatActivity() {

    lateinit var shoppingAdapter: ShoppingAdapter
    lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI() {
        setCartListVisibility(false)
        cartAdapter =
            CartAdapter(arrayListOf())
        cartAdapter.setOnItemClickListener(object :
            CartItemClickListener {
            //TODO Alka, format code before checkin :)- I have done it for now
            override fun undoToShoppingList(pos: Int, name: String) {
                shoppingAdapter.addData(name)
                setCartListVisibility(cartAdapter.itemCount > 0)
                updateBadgeCount()
            }
        })
        rv_cart.layoutManager = LinearLayoutManager(this)
        rv_cart.adapter = cartAdapter
        rv_cart.addItemDecoration(
            DividerItemDecoration(
                this,
                (rv_cart.layoutManager as LinearLayoutManager).orientation
            )
        )


        shoppingAdapter =
            ShoppingAdapter(arrayListOf())
        shoppingAdapter.setOnItemClickListener(object :
            ShoppingItemClickListener {
            override fun onAddToCart(pos: Int, name: String) {
                cartAdapter.addCartItem(name)
                setCartListVisibility(cartAdapter.itemCount > 0)
                updateBadgeCount()
            }

            override fun onDelete(pos: Int, name: String) {
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
                if (event.action === KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    addToShoppingList()
                    et_enter_item.requestFocus()
                    return true
                }
                return false
            }
        })
    }

    private fun addToShoppingList() {
        shoppingAdapter.addData(et_enter_item.text.toString())
        et_enter_item.setText("")
        updateBadgeCount()
    }

    private fun updateBadgeCount() {
        if (shoppingAdapter.itemCount > 0)
            cl_shopping.visibility = View.VISIBLE
        else
            cl_shopping.visibility = View.GONE
        layout_add_icon.tv_count.text = shoppingAdapter.itemCount.toString()
        tv_cart_count.text = cartAdapter.itemCount.toString()
    }

    //TODO please avoid single char variables, use meaningful names - check rest of the code too. - I have done it for now :)
    private fun setCartListVisibility(visible: Boolean) {
        if (visible) {
            cl_cart.visibility = View.VISIBLE
        } else {
            cl_cart.visibility = View.GONE
        }
    }
}