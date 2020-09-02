package `in`.onenzeros.shopping_list.adapter

import `in`.onenzeros.shopping_list.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_shopping_list_item.view.*


class CartAdapter(private val myDataset: ArrayList<String>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

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
    }

    override fun getItemCount() = myDataset.size

    fun addCartItem(responses: String) {
        notifyItemInserted(myDataset.size-1)
    }
}

