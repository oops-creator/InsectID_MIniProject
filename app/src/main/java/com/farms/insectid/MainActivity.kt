package com.farms.insectid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    val job = Job()
    var bitmap: Bitmap? = null
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private var modelloaded = false
    val classifier = InsectClassifier(this)
    var identified = ""

    init {
        uiScope.launch {
            withContext(Dispatchers.IO){
                classifier.initializeInterpreter()
            }
        }
    }


    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


    fun isWriteStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("Main", "Permission is granted2")
                true
            } else {
                Log.v("Main", "Permission is revoked2")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    2
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Main", "Permission is granted2")
            true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        findViewById<ImageView>(R.id.from_storage).setOnClickListener {

            val OpenImage = Intent(Intent.ACTION_GET_CONTENT)
            OpenImage.type = "*/*"

            val grant = isWriteStoragePermissionGranted()
            if(grant == false){
                Toast.makeText(this  , "Permission Not Granted" , Toast.LENGTH_SHORT).show()
            }
            val activities: List<ResolveInfo> = packageManager.queryIntentActivities(OpenImage, 0)
            val isIntentSafe: Boolean = activities.isNotEmpty()


            if (isIntentSafe) {
                startActivityForResult(OpenImage, 0)
            }
        }

        findViewById<ImageView>(R.id.from_camera).setOnClickListener {
            dispatchTakePictureIntent()
        }
        findViewById<Button>(R.id.detect).setOnClickListener {
            bitmap?.let { it1 -> classify(it1) }
        }

    }

    fun classify(bitmap: Bitmap){
        if(bitmap != null ) {
            uiScope.launch {
                findViewById<RelativeLayout>(R.id.loadingPanel).setVisibility(View.VISIBLE)
                withContext(Dispatchers.IO) {
                    identified = classifier.classify(bitmap)
                }
                Log.e("identity", identified)
                findViewById<TextView>(R.id.this_insect_text).text = identified
                findViewById<RelativeLayout>(R.id.loadingPanel).setVisibility(View.GONE)
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) {
            return;
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            bitmap = data?.extras?.get("data") as Bitmap
            val draw = BitmapDrawable(resources, bitmap)
            findViewById<ImageView>(R.id.insect_image).background = draw


        }
        else {
            val retUri: Uri? = data?.data

            try {

                if(Build.VERSION.SDK_INT < 28) {
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, retUri)
                }else{
                    val source = retUri?.let { ImageDecoder.createSource(this.contentResolver, it) }
                    bitmap = source?.let { ImageDecoder.decodeBitmap(it) }!!

                }
                val inputStream: InputStream? = retUri?.let { contentResolver.openInputStream(it) }
                val draw = Drawable.createFromStream(inputStream, retUri.toString())
                findViewById<ImageView>(R.id.insect_image).background = draw



            } catch (e: FileNotFoundException) {
                //TODO add a function

            }
           
           


        }



    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
