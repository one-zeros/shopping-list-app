package `in`.onenzeros.shoppinglist.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShoppingModel(val category: String,  val name: String, val order: Int) :
    Parcelable
