package com.example.rice_detection

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rice_detection.ml.Vgg16
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


@Composable
fun ImageUploadScreen(
    viewModel: ImageUploadViewModel = viewModel()
) {
    val context = LocalContext.current
    val classes = listOf<String>("Brown Spot", "Healthy", "Hispa", "Leaf Blast")
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            imageBitmap.value = bitmap
        }
    }

    val pickFromGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(it)
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            imageBitmap.value = bitmap
        }
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopAppBar(title = { Text(text = "Rice Disease Detection") })

        if (imageBitmap.value != null) {
            Image(
                bitmap = imageBitmap.value!!.asImageBitmap(),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.placeholder),
                contentDescription = "Placeholder Image",
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }

        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            val result = remember { mutableStateOf("") }
            Button(onClick = {
                if (imageBitmap.value != null) {
                    val resizedImage = Bitmap.createScaledBitmap(imageBitmap.value!!, 175, 175, true);

                    val model = Vgg16.newInstance(context)
                    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 175, 175, 3), DataType.FLOAT32)
                    val tensorImage = TensorImage(DataType.FLOAT32)
                    tensorImage.load(resizedImage)
                    val byteBuffer = tensorImage.buffer

                    inputFeature0.loadBuffer(byteBuffer)
                    val outputs = model.process(inputFeature0)
                    val outputFeature0: TensorBuffer = outputs.outputFeature0AsTensorBuffer

                    val maxIndex = findMaxIndex(outputFeature0.floatArray)

                    result.value = classes[maxIndex]

                    model.close()
                } else {
                    Toast.makeText(
                        context,
                        "Please select picture or take picture",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }) {
                Text(text = "Predict Disease")
            }
            if (result.value != "") {
                Text(text = result.value, style = TextStyle(fontSize = 20.sp), modifier = Modifier.padding(top = 20.dp))
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { takePhotoLauncher.launch(null) }) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Take Photo")
            }

            Button(onClick = { pickFromGalleryLauncher.launch("image/*") }) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = "Pick from Gallery")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Pick from Gallery")
            }
        }
    }
}

fun findMaxIndex(array: FloatArray): Int {

    var maxIndex = 0
    var maxValue = array[0]

    for (i in 1 until array.size) {
        if (array[i] > maxValue) {
            maxValue = array[i]
            maxIndex = i
        }
    }

    return maxIndex
}


