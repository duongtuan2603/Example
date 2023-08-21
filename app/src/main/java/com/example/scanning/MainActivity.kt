package com.example.scanning

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.example.scanning.utils.UploadPhotoFragment
import com.example.scanning.utils.showCustomToast

class MainActivity : AppCompatActivity() {
    private val mViewModel:MainViewModel by viewModels()
    private var progressDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initProgress()
        initObserve()
        setContentView(R.layout.activity_main)
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.blueColorMain)

    }

    private fun initObserve() {
        mViewModel.errorLiveData.observe(this) {
            it.getContentIfNotHandled()?.let { err ->
                this.showCustomToast(err,false)
            }
        }
        mViewModel.viewStateLiveData.observe(this) {
            if (it == ActivityViewState.LOADING) showProgress() else hideProgress()
        }
    }

    fun logOut(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    private fun initProgress() {
        val view: View = LayoutInflater.from(this).inflate(
            R.layout.dialog_progress, null
        )
        progressDialog = Dialog(this, android.R.style.Theme_Black).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(
                R.color.ripple_black
            )
            setContentView(view)
        }
    }
    fun showProgress() {
        progressDialog?.apply {
            if (isShowing) return
            show()
        }
    }

    fun hideProgress() {
        progressDialog?.apply {
            if (isShowing) {
                dismiss()
            }
        }
    }
}