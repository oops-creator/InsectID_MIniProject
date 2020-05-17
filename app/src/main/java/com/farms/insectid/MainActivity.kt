package com.farms.insectid

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.FileNotFoundException
import java.io.InputStream


class MainActivity : AppCompatActivity() {


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
                val inputStream: InputStream? = retUri?.let { contentResolver.openInputStream(it) }
                val draw = Drawable.createFromStream(inputStream, retUri.toString())
                findViewById<ImageView>(R.id.insect_image).background = draw

            } catch (e: FileNotFoundException) {
                //TODO add a function

            }
           
           


        }



    }
}
