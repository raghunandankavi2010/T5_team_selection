package com.example.raghu.tiger5regulars.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber

class MapsActivityViewModel :ViewModel() {
    private val fetchDataRepository: DownloadRepository = DownloadRepository()
    val data = MutableLiveData<List<List<HashMap<String, String>>>>()

    fun download(url: String) {
        Timber.i("Url is $url")
        viewModelScope.launch {
            val jsonString = async(Dispatchers.IO) { fetchDataRepository.fetchData(url) }
            val parseData = async(Dispatchers.IO) { fetchDataRepository.parseData(jsonString.await())  }
            val parsedData = parseData.await()
            data.value = parsedData
        }
    }
}