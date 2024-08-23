package com.manifestasi.mykklunissula.data.repository

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.ScanType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FormkklRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun uploadImage(scanType: ScanType, imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val ref = firebaseStorage.reference.child("images/$uid/${scanType.name.lowercase()}_${System.currentTimeMillis()}.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        }
    }

    suspend fun saveDataToFirestore(data: Map<String, String?>,collection:String) {
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            firebaseFirestore.collection(collection).document(uid).set(data).await()
        }
    }

    suspend fun getDataFromFirestore(): Map<String, Any>? {
        return withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            firebaseFirestore.collection("daftar_KKLluarnegeri").document(uid).get().await().data
        }
    }

    suspend fun updateDataInFirestore(collection: String, data: MutableMap<String, Any>) {
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            firebaseFirestore.collection(collection).document(uid).update(data).await()
        }
    }

    suspend fun updateImage(scanType: ScanType, imageUri: Uri, previousUrl: String?): String {
        return withContext(Dispatchers.IO) {
            // Delete previous image if exists
            previousUrl?.let {
                firebaseStorage.getReferenceFromUrl(it).delete().await()
            }

            // Upload new image
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val ref = firebaseStorage.reference.child("images/$uid/${scanType.name.lowercase()}_${System.currentTimeMillis()}.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        }
    }

    suspend fun deleteDataFromFirestore(collection: String) {
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            firebaseFirestore.collection(collection).document(uid).delete().await()
        }
    }

    suspend fun deleteAllImages() {
        withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val storageRef = firebaseStorage.reference.child("images/$uid")

            // Ambil daftar semua gambar di folder
            val listResult = storageRef.listAll().await()

            // Hapus gambar satu per satu
            for (file in listResult.items) {
                try {
                    file.delete().await()
                } catch (e: Exception) {
                    // Tangani jika terjadi error pada penghapusan file tertentu
                    Log.e("FormkklRepository", "Failed to delete file: ${file.path}", e)
                }
            }
        }
    }






}