package com.farms.insectid

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


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
           
           


        }



    }
}
