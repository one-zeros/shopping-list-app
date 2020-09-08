package `in`.onenzeros.shoppinglist.listener

interface ShoppingItemClickListener {
    fun onAddToCart(pos :Int, name : String)
    fun onDelete(pos :Int, name : String)
    fun undoToShoppingList(pos :Int, name : String)
}