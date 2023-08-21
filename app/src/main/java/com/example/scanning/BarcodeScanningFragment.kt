package com.example.scanning

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.scanning.databinding.FragmentScanBarcodeBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeScanningFragment : Fragment() {
    private var mBinding: FragmentScanBarcodeBinding? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val mActivityViewModel: MainViewModel by activityViewModels()
    private lateinit var importLauncher: ActivityResultLauncher<String>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Initialize the ActivityResultLauncher for photo import
        importLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    // Load the selected photo into the ImageView
                    val bitmap =
                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
                            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                        } else {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(
                                    requireContext().contentResolver, uri
                                )
                            )
                        }

                    // Use ML Kit to scan the barcode in the photo
                    val image = InputImage.fromBitmap(bitmap, 0)
                    val scanner = BarcodeScanning.getClient()
                    scanner.process(image).addOnSuccessListener { barcodes ->
                        // Process the detected barcodes
                        if (barcodes.isNullOrEmpty().not()) {
                            barcodes.first {
                                !it.rawValue.isNullOrEmpty()
                            }.rawValue?.let { string ->
                                mActivityViewModel.barcodeValue.value = string
                                findNavController().popBackStack()
                            }
                        }
                    }.addOnFailureListener { exception ->
                        // Handle any errors
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentScanBarcodeBinding.inflate(inflater, container, false)
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(CAMERA)
        } else {
            context?.let { startCamera(it) }
        }
    }

    private fun initListener() {
        mBinding?.apply {
            btImportPhoto.setOnClickListener {
                importLauncher.launch("image/*")
            }
            btBack.setOnClickListener { findNavController().popBackStack() }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera(ct: Context) {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ct)) { image ->
            // Run the barcode scanner on the camera frame
            val inputImage = InputImage.fromMediaImage(
                image.image!!, image.imageInfo.rotationDegrees
            )
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage).addOnSuccessListener { barcodes ->

                // Handle successful barcode scans here
                if (barcodes.isEmpty().not() && barcodes.size == 1) {
                    barcodes.first {
                        !it.rawValue.isNullOrEmpty()
                    }.rawValue?.let { string ->
                        Log.d("tuandm", "set value: $string")
                        requireActivity().runOnUiThread {
                            findNavController().popBackStack()
                        }

                        mActivityViewModel.barcodeValue.value = string
                    }
                }
            }.addOnFailureListener { exception ->
                // Handle errors here
                Toast.makeText(ct, exception.message, Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener {
                image.close()
            }
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(ct)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                mBinding?.apply {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            }

            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(ct))
    }

    override fun onStop() {
        super.onStop()
        cameraProvider?.unbindAll()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            context?.let { startCamera(it) }
        }
    }
}