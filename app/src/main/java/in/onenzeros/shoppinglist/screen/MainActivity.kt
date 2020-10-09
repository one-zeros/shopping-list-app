package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.ShoppingListApp
import `in`.onenzeros.shoppinglist.adapter.ShoppingListAdapter
import `in`.onenzeros.shoppinglist.data.model.DefaultListResponse
import `in`.onenzeros.shoppinglist.data.model.ShoppingModel
import `in`.onenzeros.shoppinglist.data.model.SuggestionListResponse
import `in`.onenzeros.shoppinglist.databinding.ActivityMainBinding
import `in`.onenzeros.shoppinglist.enum.UpdateType
import `in`.onenzeros.shoppinglist.listener.ShoppingItemClickListener
import `in`.onenzeros.shoppinglist.rest.request.UpdateListRequest
import `in`.onenzeros.shoppinglist.utils.BaseActivity
import `in`.onenzeros.shoppinglist.utils.PreferenceUtil
import `in`.onenzeros.shoppinglist.utils.Utility
import `in`.onenzeros.shoppinglist.viewModel.MainActivityViewModel
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_add_icon.view.*
import kotlinx.android.synthetic.main.layout_cart_icon.*
import java.util.*


//TODO Alka, please avoid _ in package name
class MainActivity : BaseActivity(), BaseActivity.ConnectionChangeListener {

    private lateinit var shoppingAdapter: ShoppingListAdapter
    private lateinit var shoppingResponse: SuggestionListResponse
    private var mShoppingList: ArrayList<ShoppingModel> = arrayListOf()
    private var mCartList: ArrayList<ShoppingModel> = arrayListOf()
    private var mUpdateList: ArrayList<UpdateListRequest> = arrayListOf()
    private var mPreferenceUtil: PreferenceUtil? = null
    private  var id: String = ""
    private  var updateTime: String = ""
    private  var emailAddress: String = "support@onenzeros.in"
    private  var emailSubject: String = "Quick Shopping List"

    private lateinit var timer: Timer
    private val noDelay = 0L
    private val everyTenSeconds = 10000L
    private var netConnected = false
    private val suggestionsList : MutableList<String> = mutableListOf()

    var binding: ActivityMainBinding? = null
    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding?.toolbar)
        viewModel =  ViewModelProvider.AndroidViewModelFactory(application)
            .create(MainActivityViewModel::class.java)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
        binding?.executePendingBindings()
        mPreferenceUtil = ShoppingListApp.mPreferenceUtil
        initUI()
        syncOfflineData()
        setConnectionChangeListener(this)
        initObservers()
        intent.data?.let {
            handleDeeplinkData(it)
        } ?: kotlin.run {
            viewModel.initializeData()
        }
    }

    private fun initObservers() {
        viewModel.newList.observe(this,
            androidx.lifecycle.Observer {
                clearListData()
                parseListData(it)
            })

        viewModel.existingList.observe(this,
            androidx.lifecycle.Observer {
                clearListData()
                parseListData(it)
            })

        viewModel.suggestionList.observe(this,
            androidx.lifecycle.Observer {
                shoppingResponse = it
                setSuggestionList(it)
            })

        viewModel.updateList.observe(this,
            androidx.lifecycle.Observer {
                mUpdateList.add(it)
            })

        viewModel.id.observe(this,
            androidx.lifecycle.Observer {
                id = it
            })

        viewModel.pendingList.observe(this,
            androidx.lifecycle.Observer {
                mShoppingList = it as ArrayList<ShoppingModel>
            })

        viewModel.cartList.observe(this,
            androidx.lifecycle.Observer {
                mCartList = it as ArrayList<ShoppingModel>
            })

        viewModel.updateTime.observe(this,
            androidx.lifecycle.Observer {
                updateTime = it
            })

        viewModel.setList.observe(this,
            androidx.lifecycle.Observer {
                setListData()
            })

        viewModel.toastMsg.observe(this,
            androidx.lifecycle.Observer {
                if(it == "something_went_wrong")
                    Toast.makeText(this@MainActivity, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(this@MainActivity, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show()
            })
    }

    private fun handleDeeplinkData(uri: Uri) {
        Log.e("handleDeeplinkData", "$intent, ${intent.data}")
        id = uri.getQueryParameter("id").toString()
        id.isNotEmpty().apply {
            clearListData()
            viewModel.initializeDataFromDeeplink(id)
        }
    }

    private fun syncOfflineData() {
        viewModel.syncPendingList()
        syncFromServer()
    }

    private fun setSuggestionAdapter() {
        val adapter = ArrayAdapter(this,
            R.layout.adapter_suggestion_list_item, suggestionsList)
        et_enter_item.setAdapter(adapter)
        et_enter_item.setOnItemClickListener { _, _, _, _ ->
            addToShoppingList()
        }
    }

    private fun setSuggestionList(shoppingResponse: SuggestionListResponse) {
        suggestionsList.clear()
        shoppingResponse.forEachIndexed { _, shoppingCategory ->
            suggestionsList.addAll(shoppingCategory.items)
        }
        setSuggestionAdapter()
    }

    private fun initUI() {
        shoppingAdapter =
            ShoppingListAdapter(this, mShoppingList, mCartList)
        shoppingAdapter.setOnItemClickListener(object :
            ShoppingItemClickListener {
            override fun onAddToCart(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                viewModel.updateListAPICall(UpdateListRequest(id,UpdateType.PICKED.toString(),shoppingModel.name),false)
            }
            override fun onDelete(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                viewModel.updateListAPICall(UpdateListRequest(id,UpdateType.REMOVE.toString(),shoppingModel.name),false)
            }
            override fun undoToShoppingList(pos: Int, shoppingModel: ShoppingModel) {
                updateBadgeCount()
                viewModel.updateListAPICall(UpdateListRequest(id,UpdateType.DROPPED.toString(),shoppingModel.name),false)
            }
        })

        binding?.rvShopping?.layoutManager = LinearLayoutManager(this)
        binding?.rvShopping?.adapter = shoppingAdapter
        binding?.rvShopping?.addItemDecoration(
            DividerItemDecoration(
                this,
                (binding?.rvShopping?.layoutManager as LinearLayoutManager).orientation
            )
        )

        binding?.layoutAddIcon?.ivAdd?.setOnClickListener {
            addToShoppingList()
        }

        binding?.ivDone?.setOnClickListener {
            clearListAlert()
        }

        binding?.etEnterItem?.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // If the event is a key-down event on the "enter" button
                if (event.action === ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    addToShoppingList()
                    binding?.etEnterItem?.requestFocus()
                    return true
                }
                return false
            }
        })
    }

    private fun clearListData() {
        mShoppingList.clear()
        mCartList.clear()
        shoppingAdapter.cleaData()
    }

    private fun addToShoppingList() {
        val itemName  = et_enter_item.text.toString()
        if (itemName.isNotEmpty()) {
            if(itemName.trim().isNotEmpty()) {
                groupItemByCategory(itemName.trim())?.let {
                    shoppingAdapter.addShoppingListItem(it)
                    et_enter_item.setText("")
                    updateBadgeCount()
                    viewModel.updateListAPICall(UpdateListRequest(id, UpdateType.ADD.toString(), itemName.trim()),false)
                }
            } else{
                Toast.makeText(this, getString(R.string.valid_shopping_item),Toast.LENGTH_LONG).show()
            }
        }
        else {
            Toast.makeText(this, getString(R.string.empty_shopping_item),Toast.LENGTH_LONG).show()
        }
    }

    private fun groupItemByCategory(itemName: String) : ShoppingModel? {
        var shoppingModel: ShoppingModel? = null
        shoppingResponse.forEachIndexed { _, shoppingCategory ->
            if (shoppingCategory.items.contains(itemName)) {
                shoppingModel =
                    ShoppingModel(
                        shoppingCategory.category,
                        itemName,
                        shoppingCategory.order
                    )
                return shoppingModel
            }
            else
                shoppingModel =
                    ShoppingModel(
                        "Others",
                        itemName,
                        0
                    )
        }
        return shoppingModel
    }

    private fun updateBadgeCount() {
        layout_add_icon.tv_count.text = shoppingAdapter.getShoppingListItemCount().toString()
        tv_cart_count?.text = shoppingAdapter.getCartListItemCount().toString()
 }

    private fun parseListData(defaultListResponse: DefaultListResponse) {
        val pendingList = defaultListResponse.pending.distinct()
        pendingList.forEach {
            groupItemByCategory(it)?.let {
                    shoppingModel ->  mShoppingList.add(shoppingModel)
            }
        }
        val cartList = defaultListResponse.cart.distinct()
        cartList.forEach {
            groupItemByCategory(it)?.let {
                    shoppingModel ->  mCartList.add(shoppingModel)
            }
        }
        updateTime = getString(R.string.last_updated_on_holder,Utility.getDate(defaultListResponse.lastUpdated))
        id = defaultListResponse.id
        setListData()
    }

    private fun setListData() {
        shoppingAdapter.changeData(mShoppingList,mCartList)
        updateBadgeCount()
        tv_sync_time.visibility = View.VISIBLE
        tv_sync_time.text = updateTime
        saveListToPreference(id, mShoppingList, mCartList,tv_sync_time.text.toString())
    }

    private fun saveListToPreference(id : String, mShoppingList: ArrayList<ShoppingModel>, mCartList: ArrayList<ShoppingModel>, date: String) {
        this.id = id
        mPreferenceUtil?.setListId(id)
        mPreferenceUtil?.setPendingList(Gson().toJson(mShoppingList))
        mPreferenceUtil?.setCartList(Gson().toJson(mCartList))
        mPreferenceUtil?.setLastUpdateTime(date)
    }

    override fun onNetConnectionChanged(isConnected: Boolean) {
        viewModel.setNetworkConnection(isConnected)
        if(isConnected && !netConnected) {
            netConnected = isConnected
            syncOfflineData()
        } else{
            netConnected = isConnected
        }
    }

    private fun syncFromServer() {
        val timerTask = object : TimerTask() {
            override fun run() {
                if(id.isNotEmpty() && netConnected)
                    viewModel.lodExistingList(id)
            }
        }

        timer = Timer()
        timer.schedule(timerTask, noDelay, everyTenSeconds)
    }

    override fun onPause() {
        super.onPause()
        if (::timer.isInitialized) {
            timer.cancel()
            timer.purge()
        }
      }

    override fun onStop() {
        viewModel.saveDataForOffline(id, shoppingAdapter.getShoppingList(), shoppingAdapter.getCartList(), tv_sync_time.text.toString())
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_help -> openYoutube()
            R.id.menu_story -> openWebView(WebviewActivity.SYORY_LINK)
            R.id.menu_about -> openWebView(WebviewActivity.ABOUT_LINK)
            R.id.menu_contact -> sendEmail()
            R.id.menu_share -> shareList()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openYoutube() {
        startActivity(Intent(this, YoutubePlayerActivity::class.java))
    }

    private fun openWebView(link: String) {
        startActivity(Intent(this, WebviewActivity::class.java).apply {
            putExtra(WebviewActivity.ARG_URL, link)
        })
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress \"?&subject=$emailSubject ")
        }
        startActivity(Intent.createChooser(emailIntent, "Send mail"))
    }

    private fun shareList() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareMessage = getString(R.string.share_msg,id)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearListAlert(){
        val builder = AlertDialog.Builder(this)
        with(builder)
        {
            setTitle("Do you want to create new list?")
            setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                clearListData()
                viewModel.loadNewList()
            }
            setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            show()
        }
    }
}