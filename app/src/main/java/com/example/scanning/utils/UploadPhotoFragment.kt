package com.example.scanning.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.scanning.MainViewModel
import com.example.scanning.PhotoAdapter
import com.example.scanning.databinding.FragmentUploadPhotoBinding
import kotlinx.coroutines.launch
import java.io.File


class UploadPhotoFragment : Fragment() {
    private val mActivityViewModel: MainViewModel by activityViewModels()
    private val adapter = PhotoAdapter()
    private var mBinding: FragmentUploadPhotoBinding? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    val data = result.data
                    if (data != null && data.clipData != null) {
                        val list = arrayListOf<Uri>()
                        val count = data.clipData!!.itemCount
                        for (i in 0 until count) {
                            val imageUri = data.clipData!!.getItemAt(i).uri
                            // handle the selected image(s)
                            list.add(imageUri)
                        }
                        mActivityViewModel.listImageUri.value = list
                    } else if (data != null) {
                        val imageUri = data.data
                        if (imageUri != null) {
                            val list = ArrayList<Uri>()
                            list.add(imageUri)
                            mActivityViewModel.listImageUri.value = list
                        }
                    }
                }
            }
        }
    private var currentPhotoUri: Uri? = null
    private var cameraLauncher:ActivityResultLauncher<Uri>?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean ->
            if (result) {
                lifecycleScope.launch {
                    val photoUri = currentPhotoUri
                    if (photoUri != null) {
                        val list = mActivityViewModel.listImageUri.value?: ArrayList()
                        list.add(photoUri)
                        mActivityViewModel.listImageUri.value = list
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentUploadPhotoBinding.inflate(inflater, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback = object:OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                backPress()
            }

        })
        return mBinding?.root
    }

    private fun backPress(){
        mActivityViewModel.listImageUri.value = ArrayList()
        findNavController().popBackStack()
    }

    override fun onResume() {
        super.onResume()
        initRecyclerView()
        initListener()
        initObserve()
    }

    private fun initObserve() {
        mActivityViewModel.listImageUri.observe(requireActivity()) {
            if (it.isNullOrEmpty()) {
                mBinding?.apply {
                    tvNoPhoto.visibility = View.VISIBLE
                    rcvPhoto.visibility = View.GONE
                }
            } else {
                mBinding?.apply {
                    tvNoPhoto.visibility = View.GONE
                    rcvPhoto.visibility = View.VISIBLE
                }
                adapter.setListData(it)
            }
        }
    }

    private fun initListener() {
        mBinding?.apply {
            btComplete.setOnClickListener { findNavController().popBackStack() }
            btBack.setOnClickListener { backPress() }
            btExit.setOnClickListener { backPress() }
            btImportPhotos.setOnClickListener { selectPhotos() }
            btCapture.setOnClickListener {
                if (Build.VERSION.SDK_INT<= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        manageFilePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        checkCameraAndTakePhoto()
                    }
                } else checkCameraAndTakePhoto()
            }
        }
    }

    private fun checkCameraAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            takePhoto()
        }
    }

    private fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoUri = createPhotoUri()
        currentPhotoUri = photoUri
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher?.launch(photoUri)
    }

    private fun createPhotoUri(): Uri {
        val photoFile =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "photo${System.currentTimeMillis()}.jpeg")
        if (photoFile.exists().not())
            photoFile.createNewFile()
        return FileProvider.getUriForFile(
            requireContext(), "com.example.scanning.fileprovider", photoFile
        )
    }

    private fun initRecyclerView() {
        mBinding?.apply {
            rcvPhoto.adapter = adapter
            rcvPhoto.layoutManager = GridLayoutManager(context, 4)
        }
    }

    private fun selectPhotos() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        getContent.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePhoto()
        }
    }
    private val manageFilePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkCameraAndTakePhoto()
        }
    }
}