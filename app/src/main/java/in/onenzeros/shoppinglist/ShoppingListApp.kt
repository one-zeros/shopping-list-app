package `in`.onenzeros.shoppinglist

import `in`.onenzeros.shoppinglist.utils.PreferenceUtil
import android.app.Application

class ShoppingListApp : Application() {
    companion object{
        var mPreferenceUtil : PreferenceUtil? = null
    }

    override fun onCreate() {
        super.onCreate()
        mPreferenceUtil = PreferenceUtil(this)
    }
}