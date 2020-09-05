package `in`.onenzeros.shopping_list.adapter

import `in`.onenzeros.shopping_list.R
import `in`.onenzeros.shopping_list.listener.CartItemClickListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_cart_list_item.view.*
import kotlinx.android.synthetic.main.adapter_shopping_list_item.view.tv_name


class CartAdapter(private val myDataSet: ArrayList<String>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    var cartItemClickListener : CartItemClickListener? = null

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(name: String) {
            itemView.tv_name.text = name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CartAdapter.CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_cart_list_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val name = myDataSet[position]
        holder.itemView.tv_name.text = name
        holder.itemView.iv_undo.setOnClickListener {
            removeItem(position,name)
            cartItemClickListener?.undoToShoppingList(position,name)
        }

    }

    override fun getItemCount() = myDataSet.size

    fun addCartItem(responses: String) {
        myDataSet.add(responses)
        notifyItemInserted(myDataSet.size-1)
    }

    fun removeItem(pos: Int, name: String) {
        myDataSet.remove(name)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, itemCount);
    }

    fun setOnItemClickListener( cartItemClickListener : CartItemClickListener){
        this.cartItemClickListener  = cartItemClickListener
    }
}

