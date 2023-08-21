package com.example.scanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.scanning.databinding.FragmentCreatePackageBinding
import com.example.scanning.utils.PreferenceManager
import com.example.scanning.utils.navigateWithAnimation

class CreatePackageFragment : Fragment() {
    private val mActivityViewModel: MainViewModel by activityViewModels()
    private var mBinding: FragmentCreatePackageBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCreatePackageBinding.inflate(inflater, container, false)
        return mBinding?.root
    }

    override fun onResume() {
        super.onResume()
        initObserve()
        initListener()
    }

    private fun initListener() {
        mBinding?.apply {
            btLogOut.setOnClickListener {
                context?.let { ct ->
                    PreferenceManager.removeSharedPreference(ct, "userName")
                }
                context?.let { ct ->
                    PreferenceManager.removeSharedPreference(ct, "password")
                }
                context?.let { ct ->
                    PreferenceManager.removeSharedPreference(ct, "token")
                }
                (activity as? MainActivity)?.logOut()
            }
            btScan.setOnClickListener {
                findNavController().navigateWithAnimation(R.id.barcodeScanningFragment)
            }
            btConfirm.setOnClickListener {
                context?.let { ct ->
                    PreferenceManager.getStringFromPreference(ct, "token")?.let { token ->
                        mActivityViewModel.checkBarcode(
                            token,
                            mBinding?.edtBarcodeInfo?.text?.toString() ?: ""
                        )
                    }
                }
            }
        }
    }

    private fun initObserve() {
        mActivityViewModel.barcodeValue.observe(requireActivity()) {
            mBinding?.apply {
                tvBarcodeInfo.text = it
                edtBarcodeInfo.setText(it)
            }
        }
        mActivityViewModel.checkBarcodeSuccessfully.observe(requireActivity()) {
            it.getContentIfNotHandled()?.let { success ->
                if (success) {
                    findNavController().navigateWithAnimation(R.id.editInfoFragment)
                }
            }
        }
    }
}