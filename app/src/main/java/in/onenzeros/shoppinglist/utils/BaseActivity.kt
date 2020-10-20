package `in`.onenzeros.shoppinglist.utils

import `in`.onenzeros.shoppinglist.R
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure.getString
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.util.*

open class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var mSnackBar: Snackbar? = null
    private var clientId: String = ""
    private var mConnectionChangeListener: ConnectionChangeListener? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        clientId = UUID.randomUUID().toString()
        clientId = getString(
            contentResolver,
            Settings.Secure.ANDROID_ID)
    }

    fun setConnectionChangeListener(mConnectionChangeListener: ConnectionChangeListener) {
        this.mConnectionChangeListener = mConnectionChangeListener
    }

    fun getClientId() : String {
       return clientId
    }

    private fun showMessage(isConnected: Boolean) {
        if (!isConnected) {
            val messageToUser = "Check your internet connection. Please try again later" //TODO

            mSnackBar = Snackbar.make(findViewById(R.id.rootLayout), messageToUser, Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
            mSnackBar?.duration = Snackbar.LENGTH_LONG
            mSnackBar?.show()
        } else {
            mSnackBar?.dismiss()
        }
    }


    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    /**
     * Callback will be called when there is change
     */
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showMessage(isConnected)
        mConnectionChangeListener?.let { it.onNetConnectionChanged(isConnected) }
    }

        interface ConnectionChangeListener {
            fun onNetConnectionChanged(isConnected: Boolean)
        }
}