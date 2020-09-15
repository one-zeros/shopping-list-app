package `in`.onenzeros.shoppinglist.listener

import `in`.onenzeros.shoppinglist.model.ShoppingModel

interface ShoppingItemClickListener {
    fun onAddToCart(pos :Int, shoppingModel : ShoppingModel)
    fun onDelete(pos :Int, shoppingModel : ShoppingModel)
    fun undoToShoppingList(pos :Int, shoppingModel : ShoppingModel)
}