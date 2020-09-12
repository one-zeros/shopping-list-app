package `in`.onenzeros.shoppinglist.utils

import android.content.Context
import android.content.SharedPreferences

public class PreferenceUtil(context: Context?)  {
    val FILE_NAME = "APP_PREFERENCES"
    val LIST_ID = "list_id"
    private var mPreferences: SharedPreferences? = context?.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)


    fun getListId() :String?{
        return this.mPreferences?.getString(LIST_ID, "").toString()
    }

    fun setListId( id : String) {
        this.mPreferences?.edit()?.putString(LIST_ID, id)?.apply()
    }
}