package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.adapter.ShoppingListAdapter
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_add_icon.view.*
import kotlinx.android.synthetic.main.layout_cart_icon.*

//TODO Alka, please avoid _ in package name
class MainActivity : AppCompatActivity() {

    private lateinit var shoppingAdapter: ShoppingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI() {
        shoppingAdapter =
            ShoppingListAdapter(arrayListOf(), arrayListOf())
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
        shoppingAdapter.addShoppingListItem(et_enter_item.text.toString())
        et_enter_item.setText("")
        updateBadgeCount()
    }

    private fun updateBadgeCount() {
        layout_add_icon.tv_count.text = shoppingAdapter.getShoppingListItemCount().toString()
        tv_cart_count.text = shoppingAdapter.getCartListItemCount().toString()
    }

}