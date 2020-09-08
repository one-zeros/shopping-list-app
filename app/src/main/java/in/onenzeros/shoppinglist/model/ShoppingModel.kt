package `in`.onenzeros.shoppinglist.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ShoppingModel(val type: Int,  val name: String) :
    Parcelable
