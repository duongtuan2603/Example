package com.example.scanning

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.scanning.databinding.ActivityLoginBinding
import com.example.scanning.utils.PreferenceManager
import com.example.scanning.utils.showCustomToast
import com.example.scanning.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private var mBinding: ActivityLoginBinding? = null
    private var progressDialog: Dialog? = null
    private val mViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window: Window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.blueColorMain)
        initProgress()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        setAccountLogin()
        mBinding?.apply {
            btConfirm.setOnClickListener {
                mViewModel.login(edtUsername.text.toString(),edtPassword.text.toString())
            }
        }
        initObserve()
    }

    private fun initObserve() {
        mViewModel.loginLiveData.observe(this) {
            mBinding?.apply {
                PreferenceManager.putStringToPreference(
                    this@LoginActivity, "userName", edtUsername.text.toString()
                )
                PreferenceManager.putStringToPreference(
                    this@LoginActivity, "password", edtPassword.text.toString()
                )
                PreferenceManager.putStringToPreference(
                    this@LoginActivity, "token", "${it.token_type} ${it.access_token}"
                )
                navigateToMain()
            }
        }
        mViewModel.errorLiveData.observe(this) {
            it.getContentIfNotHandled()?.let { err ->
                this.showCustomToast(err,false)
            }
        }
        mViewModel.viewStateLiveData.observe(this) {
            if (it == ActivityViewState.LOADING) showProgress() else hideProgress()
        }
    }

    private fun setAccountLogin() {
        PreferenceManager.getStringFromPreference(this, "userName")?.let {
            mBinding?.edtUsername?.setText(it)
        }
        PreferenceManager.getStringFromPreference(this, "password")?.let {
            mBinding?.edtPassword?.setText(it)
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
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