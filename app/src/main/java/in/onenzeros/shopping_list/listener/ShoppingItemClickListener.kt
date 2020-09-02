package `in`.onenzeros.shopping_list.listener

interface ShoppingItemClickListener {
    fun onAddToCart(pos :Int, name : String)
    fun onDelete(pos :Int, name : String)
}