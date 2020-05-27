package com.ahmed.esh7nly

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.NullPointerException


class MainActivity : AppCompatActivity() {

    private var picUri: Uri? = null
    private  var fullCode :String? = null
    private val REQUEST_CALL :Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        camera_preview.setImageDrawable(getDrawable(R.drawable.default_camera_view))
        Toast.makeText(this,after.text.toString(),Toast.LENGTH_LONG).show()

        capture_img.setOnClickListener {
            captureImage()
        }

    }

    //get code from image phase 1
    private fun getCode(v: View) {
       // val image = FirebaseVisionImage.fromMediaImage()
        if (camera_preview.drawable != null) {
            code.setText("")
            v.isEnabled = false
            val bitmap = (camera_preview.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    v.isEnabled = true
                    processResultText(firebaseVisionText)
                }
                .addOnFailureListener {
                    v.isEnabled = true
                    code.setText("Failed")
                }
        } else {
            Toast.makeText(this, "Select an Image First", Toast.LENGTH_LONG).show()
        }
    }

    //get code from image phase 2
    private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            code.setText("No Text Found")
            return
        }
        for (block in resultText.textBlocks) {
            val blockText = block.text
            if (blockText.equals("\u2212")){
                Toast.makeText(this,"dash",Toast.LENGTH_LONG).show()
            }else{
                code.append(blockText + "\n")
            }
        }
        fullCode= before.text.toString()+code.text.toString().replace("-", "")+after.text.toString()
        Toast.makeText(this,fullCode,Toast.LENGTH_LONG).show()

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE),101 )

        }
        processCode()
    }

    //start calling phase 1
    private fun processCode(){
        val callIntent: Intent = Uri.parse("tel:"+Uri.encode(fullCode)).let { number ->
            Intent(Intent.ACTION_CALL, number)
        }
        startActivity(callIntent)
    }

    // capture and crop Image
    private fun captureImage() {

        try {

            CropImage.activity().start(this)

        }catch(anfe : ActivityNotFoundException){
            //display an error message
            Toast.makeText(this, "Whoops your device doesn't support capturing images!", Toast.LENGTH_SHORT).show()
        }
    }


    // get img from intent and display
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result : CropImage.ActivityResult = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {

                picUri=result.uri
                camera_preview.setImageURI(picUri)
                getCode(camera_preview)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "can't read from image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override  fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CALL) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                processCode()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
