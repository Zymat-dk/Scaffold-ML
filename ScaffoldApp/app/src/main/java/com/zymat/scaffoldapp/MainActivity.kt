package com.zymat.scaffoldapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.core.widget.TintableCompoundButton
import com.zymat.scaffoldapp.databinding.ActivityMainBinding
import com.zymat.scaffoldapp.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {

    val paint = Paint()
    lateinit var imageView: ImageView
    lateinit var button: Button
    lateinit var bitmap: Bitmap
    lateinit var model: SsdMobilenetV11Metadata1
    lateinit var labels: List<String>
    val imageProcessor =
        ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)

        labels = FileUtil.loadLabels(this, "labels.txt")
        model = SsdMobilenetV11Metadata1.newInstance(this)
        imageView = findViewById(R.id.imageV)
        button = findViewById(R.id.btn)

        button.setOnClickListener {
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            var uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            get_predictions();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()  // Releases model resources if no longer used.
    }

    fun get_predictions() {
        // Creates inputs for reference.
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        // Runs model inference and gets result.
        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

        var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val h = mutable.height
        val w = mutable.width

        paint.textSize = h/15f
        paint.strokeWidth = h/85f
        var x = 0
        scores.forEachIndexed { index, fl ->
            x = index
            x *= 4
            if (fl > 0.5) {
                paint.style = Paint.Style.STROKE
                canvas.drawRect(
                    RectF(
                        locations.get(x + 1) * w,
                        locations.get(x) * h,
                        locations.get(x + 3) * w,
                        locations.get(x + 2) * h
                    ), paint
                )
                paint.style = Paint.Style.FILL
                canvas.drawText(labels.get(classes.get(index).toInt()) + " " + fl.toString(), locations.get(x+1)*w, locations.get(x)*h, paint)
            }
        }
        imageView.setImageBitmap(mutable)
    }
}