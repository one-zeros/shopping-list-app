package `in`.onenzeros.shoppinglist.utils

import android.content.Context
import android.content.SharedPreferences

public class PreferenceUtil  {
    private var mPreferences: SharedPreferences? = null
    val FILE_NAME = "APP_PREFERENCES"
    val DEFAULT_LIST = "default_list"

    fun PreferenceUtil(context: Context?) {
        this.mPreferences = context?.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)
    }

    fun getDefaultList() :String{
        return this.mPreferences?.getString(DEFAULT_LIST, null).toString()
    }

    fun setDefaultList( id : String) {
        this.mPreferences?.edit()?.putString(DEFAULT_LIST, id)?.apply()
    }
}