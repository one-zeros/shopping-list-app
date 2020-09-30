package `in`.onenzeros.shoppinglist.data.model

data class DefaultListResponse(
    val cart: List<String>,
    val created: Long,
    val id: String,
    val lastUpdated: Long,
    val pending: List<String>
)