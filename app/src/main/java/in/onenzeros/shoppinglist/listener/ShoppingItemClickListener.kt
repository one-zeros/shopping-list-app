package `in`.onenzeros.shoppinglist.listener

import `in`.onenzeros.shoppinglist.model.ShoppingModel

interface ShoppingItemClickListener {
    fun onAddToCart(pos :Int, name : ShoppingModel)
    fun onDelete(pos :Int, name : ShoppingModel)
    fun undoToShoppingList(pos :Int, name : ShoppingModel)
}