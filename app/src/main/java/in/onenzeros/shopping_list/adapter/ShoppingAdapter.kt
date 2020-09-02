package `in`.onenzeros.shopping_list.adapter

import `in`.onenzeros.shopping_list.R
import `in`.onenzeros.shopping_list.listener.ShoppingItemClickListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_shopping_list_item.view.*


class ShoppingAdapter(private val mResponseList: ArrayList<String>) :
    RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder>() {

    var shoppingItemClickListener : ShoppingItemClickListener? = null
    class ShoppingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(name: String) {
            itemView.tv_name.text = name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ShoppingAdapter.ShoppingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_shopping_list_item, parent, false)
        return ShoppingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        val name = mResponseList[position]
        holder.itemView.tv_name.text = name
        holder.itemView.iv_add_to_cart.setOnClickListener { shoppingItemClickListener?.onAddToCart(position, name) }
        holder.itemView.iv_delete.setOnClickListener { shoppingItemClickListener?.onDelete(position, name) }
    }

    fun addData(responses: String) {
        notifyItemInserted(mResponseList.size-1)
    }

    fun setOnItemClickListener(shoppingItemClickListener: ShoppingItemClickListener){
        this.shoppingItemClickListener  = shoppingItemClickListener
    }
    override fun getItemCount() = mResponseList.size
}

