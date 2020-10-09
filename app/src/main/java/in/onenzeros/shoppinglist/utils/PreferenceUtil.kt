package `in`.onenzeros.shoppinglist.utils

import android.content.Context
import android.content.SharedPreferences

public class PreferenceUtil(context: Context?)  {
    val FILE_NAME = "APP_PREFERENCES"
    val LIST_ID = "list_id"
    val LIST = "list"
    val SUGGESTION_LIST = "suggestion_list"
    val PENDING_LIST = "pending_list"
    val CART_LIST = "cart_list"
    val PENDING_UPDATE_LIST = "pending_update_list"
    val LAST_UPDATED_TIME = "last_updated_time"
    private var mPreferences: SharedPreferences? = context?.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)

    fun getListId() :String?{
        return this.mPreferences?.getString(LIST_ID, "").toString()
    }

    fun setListId( id : String) {
        this.mPreferences?.edit()?.putString(LIST_ID, id)?.apply()
    }

    // TODO is this dead code?

    fun getPendingList() :String?{
        return this.mPreferences?.getString(PENDING_LIST, "").toString()
    }

    fun setPendingList( list : String) {
        this.mPreferences?.edit()?.putString(PENDING_LIST, list)?.apply()
    }

    fun getCartList() :String?{
        return this.mPreferences?.getString(CART_LIST, "").toString()
    }

    fun setCartList( list : String) {
        this.mPreferences?.edit()?.putString(CART_LIST, list)?.apply()
    }

    fun getSuggestionList() :String?{
        return this.mPreferences?.getString(SUGGESTION_LIST, "").toString()
    }

    fun setSuggestionList( list : String) {
        this.mPreferences?.edit()?.putString(SUGGESTION_LIST, list)?.apply()
    }

    fun getPendingUpdateList() :String?{
        return this.mPreferences?.getString(PENDING_UPDATE_LIST, "").toString()
    }

    fun setPendingUpdateList(list : String) {
        this.mPreferences?.edit()?.putString(PENDING_UPDATE_LIST, list)?.apply()
    }

    fun getLastUpdateTime() :String?{
        return this.mPreferences?.getString(LAST_UPDATED_TIME, "").toString()
    }

    fun setLastUpdateTime(date : String) {
        this.mPreferences?.edit()?.putString(LAST_UPDATED_TIME, date)?.apply()
    }
}