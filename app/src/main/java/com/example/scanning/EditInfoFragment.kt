package com.example.scanning

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.scanning.databinding.FragmentEditInfoBinding
import com.example.scanning.utils.PreferenceManager
import com.example.scanning.utils.createUploadBody
import com.example.scanning.utils.navigateWithAnimation
import com.example.scanning.utils.showCustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditInfoFragment : Fragment() {
    private val mActivityViewModel: MainViewModel by activityViewModels()
    private var mBinding: FragmentEditInfoBinding? = null
    private var mAdapter: PhotoAdapter = PhotoAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentEditInfoBinding.inflate(inflater, container, false)
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initRecyclerview()
    }

    override fun onResume() {
        super.onResume()
        initObserve()
    }

    private fun initObserve() {
        mActivityViewModel.barcodeValue.observe(requireActivity()) {
            mBinding?.tvPackageInfo?.text = context?.resources?.getString(R.string.package_info, it)
        }
        mActivityViewModel.listImageUri.observe(requireActivity()) {
            mAdapter.setListData(it)
        }
        mActivityViewModel.createPackageSuccessfully.observe(requireActivity()) {
            if (it?.getContentIfNotHandled() == true) {
                context?.showCustomToast("Create Package Successfully",true)
                activity?.apply {
                    val intent = Intent()
                    intent.setClassName("com.example.scanning", "com.example.scanning.MainActivity")
                    startActivity(intent)
                    finish()
                }


            }
        }
    }

    private fun initRecyclerview() {
        mBinding?.apply {
            rcvPhoto.adapter = mAdapter
            rcvPhoto.layoutManager = GridLayoutManager(context, 4)
        }
    }

    private fun initListener() {
        mBinding?.apply {
            tvImportPhoto.setOnClickListener {
                findNavController().navigateWithAnimation(R.id.uploadPhotoFragment)
            }
            btBack.setOnClickListener { findNavController().popBackStack() }
            btConfirm.setOnClickListener {
                activity?.let { ct ->
                    PreferenceManager.getStringFromPreference(ct, "token")?.let { token ->
                        if (mActivityViewModel.listImageUri.value.isNullOrEmpty() || edtQuantity.text.toString()
                                .isEmpty()
                        ) {
                            ct.showCustomToast("Required information is empty", false)
                        } else {
                            lifecycleScope.launch(Dispatchers.IO) {
                                mActivityViewModel.setViewState(ActivityViewState.LOADING)
                                mActivityViewModel.createPackage(
                                    token,
                                    ct.createUploadBody(
                                        mActivityViewModel.listImageUri.value ?: listOf()
                                    ),
                                    mActivityViewModel.barcodeValue.value ?: "",
                                    edtNote.text.toString(),
                                    edtQuantity.text.toString(),
                                    edtWeight.text.toString(),
                                    edtShippingFee.text.toString()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}