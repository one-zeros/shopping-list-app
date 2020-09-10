package `in`.onenzeros.shoppinglist.model

class ShoppingItemResponse : ArrayList<ShoppingItemResponseItem>()

data class ShoppingItemResponseItem(
    val category: String,
    val items: List<String>,
    val order: Int
)