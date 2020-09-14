package `in`.onenzeros.shoppinglist.utils

import android.content.Context
import android.content.SharedPreferences

public class PreferenceUtil(context: Context?)  {
    val FILE_NAME = "APP_PREFERENCES"
    val LIST_ID = "list_id"
    val SUGGESTION_LIST = "suggestion_list"
    private var mPreferences: SharedPreferences? = context?.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)

    fun getListId() :String?{
        return this.mPreferences?.getString(LIST_ID, "").toString()
    }

    fun setListId( id : String) {
        this.mPreferences?.edit()?.putString(LIST_ID, id)?.apply()
    }

    fun getSuggestionList() :String?{
        return this.mPreferences?.getString(SUGGESTION_LIST, "").toString()
    }

    fun setSuggestionList( list : String) {
        this.mPreferences?.edit()?.putString(SUGGESTION_LIST, list)?.apply()
    }
}