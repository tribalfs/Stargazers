package com.tribalfs.stargazers.ui.screens.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.BundleCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tribalfs.stargazers.data.model.Stargazer
import com.tribalfs.stargazers.databinding.ViewQrBottomsheetBinding
import com.tribalfs.stargazers.ui.core.util.SharingUtils.isSamsungQuickShareAvailable
import com.tribalfs.stargazers.ui.core.util.SharingUtils.share
import com.tribalfs.stargazers.ui.core.util.loadImageFromUrl
import com.tribalfs.stargazers.ui.core.util.toast
import com.tribalfs.stargazers.ui.screens.profile.ProfileActivity.Companion.KEY_STARGAZER
import java.io.File
import java.io.FileOutputStream

class QRBottomSheet : BottomSheetDialogFragment() {

    private  var _binding: ViewQrBottomsheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(stargazer: Stargazer): QRBottomSheet {
            return QRBottomSheet().apply{
                arguments = Bundle().apply {
                    putParcelable(KEY_STARGAZER, stargazer)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            setOnShowListener {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ViewQrBottomsheetBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stargazer = BundleCompat.getParcelable(
            requireArguments(),
            KEY_STARGAZER,
            Stargazer::class.java
        )!!
        binding.sgName.text = stargazer.getDisplayName()
        binding.noteTv.text = "Scan this QR code on another device to view ${stargazer.getDisplayName()}'s profile."

        binding.qrCode.apply {
            setContent(stargazer.html_url)
            loadImageFromUrl(stargazer.avatar_url)
            invalidate()
        }

        binding.quickShareBtn.apply {
            text =  if (context.isSamsungQuickShareAvailable()) "Quick Share" else "Share"
            setOnClickListener {
                val storageDir = requireContext().filesDir
                val qrImageFile = File(storageDir, "${stargazer.getDisplayName()}_qrCode_${System.currentTimeMillis()}.png")
                FileOutputStream(qrImageFile).use { out ->
                    binding.qrCode.drawable.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                qrImageFile.share(requireContext()){
                    qrImageFile.delete()
                }
            }
        }

        binding.saveBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_TITLE, "${stargazer.getDisplayName()}_qrCode_${System.currentTimeMillis()}.png")
            saveImageResultLauncher.launch(intent)
        }
    }

    private var saveImageResultLauncher  = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = binding.qrCode.drawable.toBitmap()
            requireContext().contentResolver.openOutputStream(result.data!!.data!!)?.use { outputStream ->
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    toast("Image saved successfully")
                } else {
                    toast("Failed to save image")
                }
            } ?: run {
                toast("Failed to open output stream")
            }
        }
    }


}
