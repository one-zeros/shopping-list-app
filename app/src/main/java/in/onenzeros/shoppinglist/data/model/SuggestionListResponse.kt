package `in`.onenzeros.shoppinglist.data.model

class SuggestionListResponse : ArrayList<SuggestionListItem>()

data class SuggestionListItem(
    val category: String,
    val items: List<String>,
    val order: Int
)