package `in`.onenzeros.shoppinglist.viewModel

import `in`.onenzeros.shoppinglist.utils.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WebviewActivityViewModel : ViewModel() {

    private val _onToolbarNavigationClickEvent = MutableLiveData<Event<String>>()
    val onToolbarNavigationClickEvent: LiveData<Event<String>> = _onToolbarNavigationClickEvent
    fun onToolbarNavigationClickEvent(){
        _onToolbarNavigationClickEvent.postValue(Event("navigation_click"))
    }
}