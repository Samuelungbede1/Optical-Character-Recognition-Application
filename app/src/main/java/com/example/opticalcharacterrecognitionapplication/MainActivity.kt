package com.example.opticalcharacterrecognitionapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.size
import androidx.core.util.valueIterator
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.IOException
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    val CAMERA_RQ = 100
    private lateinit var cameraButton : Button
    private lateinit var readText : TextView
    private lateinit var bitmap : Bitmap
    var TAG = "Tag"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraButton = findViewById(R.id.buttonGetImage)
        readText = findViewById(R.id.textView)
        buttonTap()
    }


    private fun buttonTap(){
        cameraButton.setOnClickListener{
            checkPermission(android.Manifest.permission.CAMERA, "Camera", CAMERA_RQ)
        }
    }

    private fun checkPermission( permission: String, name: String, requestCode: Int){
        when{
            ContextCompat.checkSelfPermission(applicationContext,permission)== PackageManager.PERMISSION_GRANTED-> {

                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this)
                //Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
            shouldShowRequestPermissionRationale(permission)-> showDialog(permission,name,requestCode)
            else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String){
            if (grantResults.isEmpty() || grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else{

            }
        }
        when(requestCode){
            CAMERA_RQ-> innerCheck("Camera")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("permission to access your $name is required to use this App")
            setTitle("permission required")
            setPositiveButton("OK") {dialog, which ->
                ActivityCompat.requestPermissions( this@MainActivity, arrayOf(permission), requestCode)
            }
        }

        builder.create().show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            var result : CropImage.ActivityResult = CropImage.getActivityResult(data)

            if(resultCode == RESULT_OK){
                var resultUri: Uri = result.uri

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    getTextFromImage(bitmap)
                } catch (e: IOException){
                    e.printStackTrace()
                }
            }

        }
    }



    fun getTextFromImage (bitmap: Bitmap) {
        var textRecognizer: TextRecognizer = TextRecognizer.Builder(this).build()
        if (!textRecognizer.isOperational){
            Toast.makeText(applicationContext, "Error Occurred", Toast.LENGTH_SHORT).show()
        } else{
            val frame : Frame = Frame.Builder().setBitmap(bitmap).build()
            var textBlockSpareArray : SparseArray<TextBlock> = textRecognizer.detect(frame)
            var stringBuilder : StringBuilder = StringBuilder()

            for (i in 0 until textBlockSpareArray.size()) {
                var textBlock: TextBlock = textBlockSpareArray.valueAt(i)
                stringBuilder.append(textBlock.value)
                stringBuilder.append("\n")
            }

            Log.d(TAG, "get text from image: $stringBuilder")
            Toast.makeText(applicationContext, "Text Read", Toast.LENGTH_SHORT).show()
            readText.setText(stringBuilder.toString())
            cameraButton.setText("Retake")
        }

    }
}