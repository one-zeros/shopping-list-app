package `in`.onenzeros.shoppinglist.adapter

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_shopping_list_item.view.*

class ShoppingAdapter(private val mResponseList: ArrayList<String>) :
    RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder>() {

    private var shoppingItemClickListener : ShoppingItemClickListener? = null
    class ShoppingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ShoppingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_shopping_list_item, parent, false)
        return ShoppingViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        val name = mResponseList[position]
        holder.itemView.tv_name.text = name
        holder.itemView.iv_add_to_cart.setOnClickListener {
            removeItem(position,name)
            shoppingItemClickListener?.onAddToCart(position, name)
        }
        holder.itemView.iv_delete.setOnClickListener {
            removeItem(position,name)
            shoppingItemClickListener?.onDelete(position, name)
        }
    }

    fun addData(responses: String) {
        mResponseList.add(responses)
        notifyItemInserted(mResponseList.size-1)
    }

    private fun removeItem(pos: Int, name: String) {
        mResponseList.remove(name)
        notifyItemRemoved(pos)
        notifyItemRangeChanged(pos, itemCount)
    }

    fun setOnItemClickListener(shoppingItemClickListener: ShoppingItemClickListener){
        this.shoppingItemClickListener  = shoppingItemClickListener
    }
    override fun getItemCount() = mResponseList.size
}

