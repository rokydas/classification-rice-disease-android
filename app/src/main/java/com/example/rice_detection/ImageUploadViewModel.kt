package com.example.rice_detection

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel

class ImageUploadViewModel : ViewModel() {
    fun onImageSelected(bitmap: Bitmap) {
        // Handle the selected image from camera
    }

    fun onImageSelectedFromGallery(bitmap: ImageBitmap) {
        // Handle the selected image from gallery
    }
}