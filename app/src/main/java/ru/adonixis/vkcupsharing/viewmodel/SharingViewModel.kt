package ru.adonixis.vkcupsharing.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import ru.adonixis.vkcupsharing.requests.VKWallPostCommand

class SharingViewModel : ViewModel() {
    companion object {
        private const val TAG = "SharingViewModel"
    }
    private var resultLiveData: MutableLiveData<Int>? = null
    private var errorMessageLiveData: MutableLiveData<String>? = null

    fun getResultLiveData(): LiveData<Int> {
        resultLiveData = MutableLiveData()
        return resultLiveData as LiveData<Int>
    }

    fun getErrorMessageLiveData(): LiveData<String> {
        errorMessageLiveData = MutableLiveData()
        return errorMessageLiveData as LiveData<String>
    }

    fun sharePost(message: String? = null, uri: Uri) {
        val photos = ArrayList<Uri>()
        uri.let {
            photos.add(it)
        }
        VK.execute(VKWallPostCommand(message, photos), object: VKApiCallback<Int> {
            override fun success(result: Int) {
                resultLiveData!!.value = result
            }

            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
                errorMessageLiveData!!.value = error.toString()
            }
        })
    }

}