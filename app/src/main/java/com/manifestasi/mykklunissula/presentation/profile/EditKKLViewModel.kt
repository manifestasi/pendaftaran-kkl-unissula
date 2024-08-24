package com.manifestasi.mykklunissula.presentation.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manifestasi.mykklunissula.data.repository.FormkklRepository
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.ScanType
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditKKLViewModel @Inject constructor(
    private val repository: FormkklRepository
) : ViewModel() {
    private val _uploadResult = MutableLiveData<Resource<Map<ScanType, String>>>()
    val uploadResult: LiveData<Resource<Map<ScanType, String>>> = _uploadResult

    private val _saveResult = MediatorLiveData<Resource<Void>>()
    val saveResult: LiveData<Resource<Void>> = _saveResult

    private val _dataResult = MutableLiveData<Resource<Map<String, Any>>>()
    val dataResult: LiveData<Resource<Map<String, Any>>> = _dataResult

    // Variabel untuk menyimpan sementara URI gambar yang dipilih
    private var selectedImages: MutableMap<ScanType, Uri?> = mutableMapOf()

    fun setSelectedImage(scanType: ScanType, imageUri: Uri?) {
        selectedImages[scanType] = imageUri
    }

    private val _deleteResult = MutableLiveData<Resource<Unit>>()
    val deleteResult: LiveData<Resource<Unit>> = _deleteResult

    private val _deleteResultimage = MutableLiveData<Resource<Unit>>()
    val deleteResultimage: LiveData<Resource<Unit>> = _deleteResultimage

    fun getDataFromFirestore(collection: String) {
        viewModelScope.launch {
            _dataResult.value = Resource.Loading
            try {
                val data = repository.getDataFromFirestore(collection)
                _dataResult.value = Resource.Success(data ?: emptyMap())
            } catch (e: Exception) {
                _dataResult.value = Resource.Error(e)
            }
        }
    }

    fun updateDataInFirestore(collection: String, data: MutableMap<String, Any>) {
        viewModelScope.launch {
            _saveResult.value = Resource.Loading
            try {
                repository.updateDataInFirestore(collection, data)
                _saveResult.value = Resource.Success(null)
            } catch (e: Exception) {
                _saveResult.value = Resource.Error(e)
            }
        }
    }

    suspend fun updateImage(collection: String,scanType: ScanType, imageUri: Uri, previousUrl: String?): String? {
        return withContext(Dispatchers.IO) {
            repository.updateImage(collection,scanType, imageUri, previousUrl)
        }
    }

    fun deleteDataFromFirestore(collection: String) {
        viewModelScope.launch {
            _deleteResult.value = Resource.Loading
            try {
                repository.deleteDataFromFirestore(collection)
                _deleteResult.value = Resource.Success(null)
            } catch (e: Exception) {
                _deleteResult.value = Resource.Error(e)
            }
        }
    }

    fun deleteImage(collection: String) {
        viewModelScope.launch {
            _deleteResultimage.value = Resource.Loading
            try {
                repository.deleteAllImages(collection)
                _deleteResultimage.value = Resource.Success(null)
            } catch (e: Exception) {
                _deleteResultimage.value = Resource.Error(e)
            }
        }
    }


}