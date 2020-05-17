package com.farms.insectid

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Job
import java.io.FileNotFoundException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    val job = Job()
    lateinit var bitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageView>(R.id.insect_image).setOnClickListener {

            val OpenImage = Intent(Intent.ACTION_GET_CONTENT)
            OpenImage.type = "*/*"


            val activities: List<ResolveInfo> = packageManager.queryIntentActivities(OpenImage, 0)
            val isIntentSafe: Boolean = activities.isNotEmpty()


            if (isIntentSafe) {
                startActivityForResult(OpenImage, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) {
            return;
        } else {
            val retUri: Uri? = data?.data

            try {
                val source = retUri?.let { ImageDecoder.createSource(this.contentResolver, it) }
                bitmap = source?.let { ImageDecoder.decodeBitmap(it) }!!
                //bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, retUri)
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
