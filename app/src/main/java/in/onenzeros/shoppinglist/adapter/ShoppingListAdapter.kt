package `in`.onenzeros.shoppinglist.adapter

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import `in`.onenzeros.shoppinglist.model.ShoppingModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.android.synthetic.main.adapter_cart_list_item.view.*
import kotlinx.android.synthetic.main.adapter_shopping_list_item.view.*
import kotlinx.android.synthetic.main.adapter_shopping_list_item.view.tv_name
import kotlinx.android.synthetic.main.layout_cart_icon.view.*


class ShoppingListAdapter(private val mShoppingList: ArrayList<ShoppingModel>, private val mCartList: ArrayList<ShoppingModel>) :
    Adapter<ViewHolder>() {

    private val VIEW_TYPE_SHOPPING = 0
    private val VIEW_TYPE_CART = 1
    private val VIEW_TYPE_HEADER_CART = 2

    private var shoppingItemClickListener : ShoppingItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        when (viewType) {
            VIEW_TYPE_SHOPPING -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_shopping_list_item, parent, false)
                return ShoppingViewHolder(
                    view
                )
            }
            VIEW_TYPE_CART -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_cart_list_item, parent, false)
                return CartViewHolder(
                    view
                )
            }
            VIEW_TYPE_HEADER_CART -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_cart_header_item, parent, false)
                return CartHeaderViewHolder(
                    view
                )
            }
            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_shopping_list_item, parent, false)
                return ShoppingViewHolder(
                    view
                )
            }
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ShoppingViewHolder -> {
                val name = mShoppingList[position].name
                holder.bind(name)
                holder.itemView.iv_add_to_cart.setOnClickListener {
                    addCartListItem(position,name)
                    shoppingItemClickListener?.onAddToCart(position, name)
                }
                holder.itemView.iv_delete.setOnClickListener {
                    deleteShoppingListItem(position,name)
                    shoppingItemClickListener?.onDelete(position, name)
                }
            }
            is CartViewHolder -> {
                val pos = position - mShoppingList.size -1
                val name = mCartList[pos].name
                holder.bind(mCartList[pos].name)
                holder.itemView.iv_undo.setOnClickListener {
                    undoCartListItem(pos,name)
                    shoppingItemClickListener?.undoToShoppingList(position, name)
                }
            }
            is CartHeaderViewHolder -> {
                holder.bind(mCartList.size)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < mShoppingList.size -> {
                VIEW_TYPE_SHOPPING
            }
            position == mShoppingList.size -> {
                VIEW_TYPE_HEADER_CART
            }
            position - mShoppingList.size <= mCartList.size -> {
                VIEW_TYPE_CART
            }
            else -> -1
        }
    }

    fun getShoppingListItemCount() : Int {
       return mShoppingList.size
    }

    fun getCartListItemCount() : Int {
       return mCartList.size
    }

    fun addShoppingListItem(responses: String) {
        mShoppingList.add(ShoppingModel(VIEW_TYPE_SHOPPING,responses))
        notifyItemInserted(mShoppingList.size-1)
    }

    private fun deleteShoppingListItem(position: Int, responses: String) {
        mShoppingList.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun addCartListItem(position: Int, responses: String) {
        mShoppingList.removeAt(position)
        mCartList.add(ShoppingModel(VIEW_TYPE_CART,responses))
        notifyDataSetChanged()
    }

    private fun undoCartListItem(position: Int, responses: String) {
        mCartList.removeAt(position)
        mShoppingList.add(ShoppingModel(VIEW_TYPE_SHOPPING,responses))
        notifyDataSetChanged()
    }

    class ShoppingViewHolder(itemView: View) : ViewHolder(itemView) {
       fun bind(name: String) {
            itemView.tv_name.text = name
        }
    }

    class CartViewHolder(itemView: View) : ViewHolder(itemView) {
       fun bind(name: String) {
            itemView.tv_name.text = name
        }
    }

    class CartHeaderViewHolder(itemView: View) : ViewHolder(itemView) {
       fun bind(count: Int) {
            itemView.tv_cart_count.text = count.toString()
        }
    }

    fun setOnItemClickListener( shoppingItemClickListener: ShoppingItemClickListener){
        this.shoppingItemClickListener  = shoppingItemClickListener
    }

    override fun getItemCount(): Int {
        return mShoppingList.size + mCartList.size+1
    }
}
