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

class ShoppingListAdapter(private var mShoppingList: ArrayList<ShoppingModel>, private var mCartList: ArrayList<ShoppingModel>) :
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
                val item = mShoppingList[position]
                holder.bind(item.name)
                holder.itemView.iv_add_to_cart.setOnClickListener {
                    addCartListItem(position,item)
                    shoppingItemClickListener?.onAddToCart(position, item)
                }
                holder.itemView.iv_delete.setOnClickListener {
                    deleteShoppingListItem(position,item)
                    shoppingItemClickListener?.onDelete(position, item)
                }
            }
            is CartViewHolder -> {
                val pos = position - mShoppingList.size -1
                val name = mCartList[pos]
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

    fun addShoppingListItem(responses: ShoppingModel) {
        mShoppingList.add(responses)
        mShoppingList = getGroupedList(mShoppingList)
        notifyDataSetChanged()
    }

    private fun deleteShoppingListItem(position: Int, responses: ShoppingModel) {
        mShoppingList.remove(responses)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, position)
    }

    private fun addCartListItem(position: Int, responses: ShoppingModel) {
        mShoppingList.remove(responses)
        mCartList.add(responses)
        mCartList = getGroupedList(mCartList)
        notifyDataSetChanged()
    }

    private fun getGroupedList(mList: ArrayList<ShoppingModel>): ArrayList<ShoppingModel> {
        val comparator
                = compareBy<ShoppingModel> { it.order }
                .thenComparator { a, b -> compareValues(a.name,b.name)}
        return ArrayList(mList.sortedWith(comparator).toList())
    }

    private fun undoCartListItem(position: Int, responses: ShoppingModel) {
        mCartList.remove(responses)
        mShoppingList.add(responses)
        mShoppingList = getGroupedList(mShoppingList)
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

    fun getShoppingList() : ArrayList<ShoppingModel> {
        return mShoppingList
    }


    fun getCartList() : ArrayList<ShoppingModel> {
        return mCartList
    }

    fun changeData(mShoppingList: ArrayList<ShoppingModel>, mCartList: ArrayList<ShoppingModel>) {
        this.mShoppingList = getGroupedList(mShoppingList)
        this.mCartList = getGroupedList(mCartList)
        notifyDataSetChanged()
    }

    fun cleaData() {
        this.mShoppingList.clear()
        this.mCartList.clear()
        notifyDataSetChanged()
    }
}

