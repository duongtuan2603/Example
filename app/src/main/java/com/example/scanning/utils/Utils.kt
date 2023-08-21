package com.example.scanning.utils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.scanning.R
import com.example.scanning.databinding.LayoutToastBinding
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream


fun NavController.navigateWithAnimation(idFragment: Int, bundle: Bundle? = null) {
    val navBuilder = NavOptions.Builder()
    navBuilder.setEnterAnim(R.anim.slide_in_left).setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_right).setPopExitAnim(R.anim.slide_out_right)
    navigate(idFragment, bundle, navBuilder.build())
}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}

fun Context.showCustomToast(content: String?, isSuccess: Boolean) {
    val toastBinding: LayoutToastBinding = LayoutToastBinding.inflate(
        LayoutInflater.from(this)
    )
    toastBinding.text.text = content
    toastBinding.icon.setImageResource(if (isSuccess) R.drawable.talk_show_ic_selected_white else R.drawable.talk_show_ic_failed)
    val toast = Toast(this)
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.duration = Toast.LENGTH_SHORT
    toast.view = toastBinding.root
    toast.show()
}

suspend fun Context.createUploadBody(uriList: List<Uri>): MultipartBody {
    return MultipartBody.Builder().apply {
        setType(MultipartBody.FORM)
        val numPhotos = uriList.size
        val targetSize = 2 * 1024 * 1024 / numPhotos
        Log.d("tuandm", "targetSize: $targetSize")
        for ((index, uri) in uriList.withIndex()) {
            val inputStream = contentResolver.openInputStream(uri)!!
            val mime = MimeTypeMap.getSingleton()
            val file = File(
                cacheDir,
                "image_$index.${mime.getExtensionFromMimeType(contentResolver.getType(uri))}"
            )
            file.copyInputStreamToFile(inputStream)

            val requestFile = (if (file.length() > targetSize) Compressor.compress(
                this@createUploadBody, file
            ) {
                quality(80/numPhotos)
            } else file).asRequestBody("image/*".toMediaTypeOrNull())
            addFormDataPart("images[$index]", file.name, requestFile)
        }
    }.build()
}
