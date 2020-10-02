package `in`.onenzeros.shoppinglist.viewModel

import `in`.onenzeros.shoppinglist.ShoppingListApp
import `in`.onenzeros.shoppinglist.data.model.DefaultListResponse
import `in`.onenzeros.shoppinglist.data.model.ShoppingModel
import `in`.onenzeros.shoppinglist.data.model.SuggestionListResponse
import `in`.onenzeros.shoppinglist.rest.ApiService
import `in`.onenzeros.shoppinglist.rest.request.UpdateListRequest
import `in`.onenzeros.shoppinglist.utils.PreferenceUtil
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.collections.ArrayList


class MainActivityViewModel : AndroidViewModel {

    private var mPreferenceUtil: PreferenceUtil? = ShoppingListApp.mPreferenceUtil
    private lateinit var app: ShoppingListApp

    constructor(application: Application) : super(application) {
        app = application as ShoppingListApp
    }

    private val _newList = MutableLiveData<DefaultListResponse>()
    val newList: LiveData<DefaultListResponse> = _newList

    private val _suggestionList = MutableLiveData<SuggestionListResponse>()
    val suggestionList: LiveData<SuggestionListResponse> = _suggestionList

    private val _existingList = MutableLiveData<DefaultListResponse>()
    val existingList: LiveData<DefaultListResponse> = _existingList

     private val _pendingList = MutableLiveData<List<ShoppingModel>>()
     val pendingList: LiveData<List<ShoppingModel>> = _pendingList

     private val _updateList = MutableLiveData<UpdateListRequest>()
     val updateList: LiveData<UpdateListRequest> = _updateList

    private val _id = MutableLiveData<String>()
    var id: LiveData<String> = _id

    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String> = _toastMsg

    private val _updateTime= MutableLiveData<String>()
    val updateTime: LiveData<String> = _updateTime

    private val _cartList = MutableLiveData<List<ShoppingModel>>()
    val cartList: LiveData<List<ShoppingModel>> = _cartList

    private val _setList = MutableLiveData<String>()
    val setList: LiveData<String> = _setList

    private var mLocalUpdateList: ArrayList<UpdateListRequest> = arrayListOf()
    private var mSyncUpdateList: ArrayList<UpdateListRequest> = arrayListOf()
    private var netConnected = false
    private var mUpdateList: ArrayList<UpdateListRequest> = arrayListOf()

    private val apiService by lazy {
        ApiService.create()
    }

    init {
        getDefaultSuggestionList()
        mLocalUpdateList = getSavedPendingUpdateList()
 }

    fun initializeDataFromDeeplink(id : String) {
        clearAllPreviousData(id)
        lodExistingList(id)
    }

    fun initializeData() {
      if(netConnected)
            fetchNetworkData()
        else
            fetchLocalData()
    }

    private fun fetchLocalData() {
        val gson = Gson()
        if(!mPreferenceUtil?.getPendingList().isNullOrEmpty() && !mPreferenceUtil?.getCartList().isNullOrEmpty() ){
            val pendingListString  = mPreferenceUtil?.getPendingList()
            val cartListString  = mPreferenceUtil?.getCartList()
            val listType = object : TypeToken<List<ShoppingModel>>() {}.type

            val id = mPreferenceUtil?.getListId().toString()
            _id.postValue(id)
            _pendingList.postValue(gson.fromJson(pendingListString, listType))
            _cartList.postValue(gson.fromJson(cartListString, listType))
            mPreferenceUtil?.getLastUpdateTime()?.let { _updateTime.postValue(it) }
            _setList.postValue("setData")
        }
    }

    private fun fetchNetworkData() {
        ShoppingListApp.mPreferenceUtil?.getListId().let{
            if(it.isNullOrEmpty()) {
                _id.postValue(it)
                loadNewList()
            } else
                lodExistingList(it)
        }
        loadSuggestionList()
    }

    fun loadNewList() = viewModelScope.launch(Dispatchers.IO) {
        val call
                = apiService.getDefaultList()

        call.enqueue(object : Callback<DefaultListResponse> {
            override fun onResponse(call: Call<DefaultListResponse>, response: Response<DefaultListResponse>) {
                if (response.code() == 200) {
                    response.body()?.let {
                        _newList.postValue(it)
                    }
                }
            }
            override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
                Log.e("defaultListAPICall","onFailure : ${t.printStackTrace()}")
                _toastMsg.postValue("something_went_wrong")
            }
        })
    }

  private fun loadSuggestionList() = viewModelScope.launch(Dispatchers.IO) {
        val call
                = apiService.getSuggestionList()

        call.enqueue(object : Callback<SuggestionListResponse> {
            override fun onResponse(call: Call<SuggestionListResponse>, response: Response<SuggestionListResponse>) {
                if (response.code() == 200) {
                    response.body()?.let {
                        val listItemType = object : TypeToken<SuggestionListResponse>() {}.type
                        mPreferenceUtil?.setSuggestionList(Gson().toJson(it,listItemType))
                        _suggestionList.postValue(it)
                    }
                }
            }
            override fun onFailure(call: Call<SuggestionListResponse>, t: Throwable) {
              }
        })
    }

    fun lodExistingList(id: String) {
        val call
                = apiService.getExistingList(id)

        call.enqueue(object : Callback<DefaultListResponse> {
            override fun onResponse(call: Call<DefaultListResponse>, response: Response<DefaultListResponse>) {
                if (response.code() == 200) {
                    response.body()?.let {
                        _existingList.postValue(it)
                    }
                }
            }
            override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
            }
        })
    }

    fun updateListAPICall(updateListRequest: UpdateListRequest, isPendingList : Boolean) {
        if(netConnected) {
            val call = apiService.updateExistingList(updateListRequest)

            call.enqueue(object : Callback<DefaultListResponse> {
                override fun onResponse(
                    call: Call<DefaultListResponse>,
                    response: Response<DefaultListResponse>
                ) {
                    if (response.code() == 200) {
                        response.body()?.let {
                            if(isPendingList) {
                                _existingList.postValue(it)
                                if(mSyncUpdateList.size>0)
                                    mSyncUpdateList.removeAt(0)
                                val updateListString = Gson().toJson(mSyncUpdateList)
                                mPreferenceUtil?.setPendingUpdateList(updateListString)
                                if(mSyncUpdateList.size>0)
                                    updateListAPICall(mSyncUpdateList[0],true)

                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DefaultListResponse>, t: Throwable) {
                    if(isPendingList)
                            updateListAPICall(updateListRequest,true)
                }
            })
        } else{
            mUpdateList.add(updateListRequest)
        }
    }

    private fun getSavedPendingUpdateList(): ArrayList<UpdateListRequest> {
        mPreferenceUtil?.getPendingUpdateList()?.let {
            if(it.isNotEmpty()){
                val gson = Gson()
                val listType = object : TypeToken<List<UpdateListRequest>>() {}.type
                val listString  = it
                return gson.fromJson(listString, listType)
            }
        }
        return arrayListOf()
    }

    private fun  getDefaultSuggestionList() {
        val gson = Gson()
        val listItemType = object : TypeToken<SuggestionListResponse>() {}.type

        val shoppingListText: String? = if(mPreferenceUtil?.getSuggestionList().isNullOrEmpty()) {
            loadJSONFromAsset()
        } else{
            mPreferenceUtil?.getSuggestionList()
        }
        val list :SuggestionListResponse = gson.fromJson(shoppingListText, listItemType)
        _suggestionList.postValue(list)
    }

    private fun loadJSONFromAsset(): String? {
        val jsonString: String
        try {
            jsonString = getApplication<ShoppingListApp>().applicationContext.assets.open("shoppingItems.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    fun syncPendingList() {
        getAllPendingUpdateList()
        this.mSyncUpdateList = mSyncUpdateList
        if(mSyncUpdateList.size>0)
            updateListAPICall(mSyncUpdateList[0],true)
    }

    fun setNetworkConnection(isNetConnected : Boolean) {
        if(netConnected != isNetConnected){
            netConnected = isNetConnected
            if (netConnected)
                fetchNetworkData()
        }
    }

    private fun getAllPendingUpdateList(): ArrayList<UpdateListRequest> {
        mSyncUpdateList.clear()
        if(mLocalUpdateList.size>0) {
            mSyncUpdateList.addAll(mLocalUpdateList)
            mLocalUpdateList.clear()
        }
        if(mUpdateList.size>0) {
            mSyncUpdateList.addAll(mUpdateList)
            mUpdateList.clear()
        }
        return mSyncUpdateList
    }

    private fun saveListToPreference(id : String, mShoppingList: ArrayList<ShoppingModel>, mCartList: ArrayList<ShoppingModel>, date: String) {
        _id.postValue(id)
        mPreferenceUtil?.setListId(id)
        mPreferenceUtil?.setPendingList(Gson().toJson(mShoppingList))
        mPreferenceUtil?.setCartList(Gson().toJson(mCartList))
        mPreferenceUtil?.setLastUpdateTime(date)
    }

    private fun savePendingUpdateList(
        mUpdateList: ArrayList<UpdateListRequest>,
        isNewList: Boolean) {
        var newList = arrayListOf<UpdateListRequest>()

        if(mUpdateList.isNotEmpty()) {
            if (isNewList && mLocalUpdateList.isNotEmpty()) {
                newList.addAll(mLocalUpdateList)
                newList.addAll(mUpdateList)
            } else {
                newList = mUpdateList
            }
        }
        val updateListString = Gson().toJson(newList)
        mPreferenceUtil?.setPendingUpdateList(updateListString)
        Log.e("mUpdateList", updateListString)

    }

    fun saveDataForOffline(
        id: String,
        shoppingList: ArrayList<ShoppingModel>,
        cartList: ArrayList<ShoppingModel>,
        syncTime: String
    ) {
        getAllPendingUpdateList()
        savePendingUpdateList(mSyncUpdateList, false)
        saveListToPreference(id, shoppingList, cartList, syncTime)
    }

    private fun clearAllPreviousData(sharedListId: String) {
        mLocalUpdateList.clear()
        mSyncUpdateList.clear()
        mUpdateList.clear()
        mPreferenceUtil?.setPendingList("")
        mPreferenceUtil?.setCartList("")
        mPreferenceUtil?.setPendingUpdateList("")
        mPreferenceUtil?.setLastUpdateTime("")
        mPreferenceUtil?.setListId(sharedListId)
    }

}