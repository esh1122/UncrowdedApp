package com.kosmo.uncrowded.retrofit.picture


import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.kosmo.uncrowded.databinding.DialogCnnBinding

class CnnDialog(
    val cnn: ResponseCnn
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
        val decodedBytes = Base64.decode(cnn.base64Plot, Base64.DEFAULT)
        binding.cnnImage.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size))
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
