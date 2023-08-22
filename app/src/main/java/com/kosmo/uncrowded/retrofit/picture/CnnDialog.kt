package com.kosmo.uncrowded.retrofit.picture


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kosmo.uncrowded.databinding.DialogCnnBinding

class CnnDialog(
    val cnn: ResponseCnn,
    val bitmap: Bitmap
) : DialogFragment() {

    private var _binding: DialogCnnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DialogCnnBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.cnnNumberOfPeople.text = "${cnn.prediction}"
        binding.cnnImage.setImageBitmap(bitmap)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
