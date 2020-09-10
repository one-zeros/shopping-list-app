package `in`.onenzeros.shoppinglist.model

data class DefaultListResponse(
    val cart: List<String>,
    val created: Long,
    val id: String,
    val lastUpdated: Long,
    val pending: List<String>
)