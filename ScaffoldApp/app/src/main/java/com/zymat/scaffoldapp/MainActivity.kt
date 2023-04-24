package com.zymat.scaffoldapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.TintableCompoundButton
import androidx.navigation.ui.AppBarConfiguration
import com.zymat.scaffoldapp.databinding.ActivityMainBinding
import com.zymat.scaffoldapp.ml.ScaffoldModel
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.Locale.Category

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var button: Button
    lateinit var bitmap: Bitmap
    lateinit var model: ScaffoldModel
    lateinit var labels: List<String>
    private val paint = Paint()
    private val imageProcessor: ImageProcessor =
        ImageProcessor.Builder().add(
            ResizeOp(
                300,
                300,
                ResizeOp.ResizeMethod.BILINEAR
            )
        ).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)

        labels = FileUtil.loadLabels(this, "labels.txt")
        model = ScaffoldModel.newInstance(this)
        imageView = findViewById(R.id.imageV)
        button = findViewById(R.id.btn)

//        button.setOnClickListener {
//            startActivityForResult(intent, 123)
//        }
    }
    fun takePhoto(view: View){
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(intent.resolveActivity(packageManager) != null){
            startActivityForResult(intent, 123)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == RESULT_OK) {
            bitmap = data?.extras?.get("data") as Bitmap
            getPredictions();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()  // Releases model resources if no longer used.
    }

    private fun getPredictions() {
        // Creates inputs for reference.
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        // Runs model inference and gets result.
        val outputs = model.process(image)
        val detectionResults = outputs.detectionResultList

        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val w = mutable.width
        val h = mutable.height
        val wScale = w / 300
        val hScale = h / 300

        paint.textSize = h / 15f
        paint.strokeWidth = h / 85f

        for (result in detectionResults) {
            // Gets result from DetectionResult.
            val location = result.locationAsRectF;
            val category = result.categoryAsString;
            val score = result.scoreAsFloat;
            if (score < 0.88) {
                continue
            }
            println("Score: $score")
            paint.color = getRectColor(category)
            paint.style = Paint.Style.STROKE
            canvas.drawRect(
                RectF(
                    location.left * wScale,
                    location.top * hScale,
                    location.right * wScale,
                    location.bottom * hScale
                ), paint
            )
        }
        imageView.setImageBitmap(mutable)
    }
    private fun getRectColor(category: String): Int {
        val color: Int = when (category){
            "Hjul" -> R.color.sea
            else -> R.color.white
        }
        return ContextCompat.getColor(this, color)
    }
}