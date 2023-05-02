package com.zymat.scaffoldapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.zymat.scaffoldapp.ml.ScaffoldModel
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {
    private lateinit var button: ImageButton  // Knap til at vælge billede
    lateinit var imageView: ImageView  // ImageView viser billedet
    lateinit var bitmap: Bitmap  // Bitmap af billedet
    lateinit var model: ScaffoldModel  // TFLite model til at detektere objekter
    lateinit var labels: List<String>  // Liste af labels til modellen ("Hjul")
    private val paint = Paint()  // Paint objekt til at tegne på billedet
    private val imageProcessor: ImageProcessor =  // Image processor til at ændre billedets størrelse
        ImageProcessor.Builder().add(
            ResizeOp(
                300,  // Billedets størrelse bliver 300x300
                300,
                ResizeOp.ResizeMethod.BILINEAR
            )
        ).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Når appen startes
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Sætter layout til activity_main.xml

        val intent = Intent()  // Intentionen om at vælge et billede
        intent.type = "image/*"  // Det skal være et billede
        intent.action = Intent.ACTION_GET_CONTENT  // Vi vil have et billede fra brugeren

        // Henter labels og model
        labels = FileUtil.loadLabels(this, "labels.txt")
        model = ScaffoldModel.newInstance(this)

        // Finder ImageView og knap
        imageView = findViewById(R.id.imageV)
        button = findViewById(R.id.btn)
        button.setOnClickListener {// Når der trykkes på knappen
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Når der er valgt et billede
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            // Hvis det er den rigtige statuskode
            val uri = data?.data  // Finder billedets URI
            // Læser billedet fra URI
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            getPredictions()  // Finder objekter i billedet
        }
    }

    override fun onDestroy() {
        // Når appen lukkes
        super.onDestroy()
        model.close()  // Lukker modellen, så den ikke bruger mere hukommelse
    }

    private fun getPredictions() {
        // Finder objekter i billedet
        // Start med at ændre billedet til et TensorImage
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)  // Ændrer billedet til 300x300

        // Finder objekter i billedet
        val outputs = model.process(image)
        // Finder resultaterne fra modellen
        val detectionResults = outputs.detectionResultList

        // Laver en kopi af billedet, så vi kan tegne på det
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable) // Man kan tegne på canvas
        val w = mutable.width  // Finder billedets bredde
        val h = mutable.height  // Finder billedets højde

        // Finder skaleringsfaktorerne for billedet
        val wScale = w / 300
        val hScale = h / 300

        // Sætter paint objektets egenskaber
        paint.textSize = h / 15f
        paint.strokeWidth = h / 85f

        for (result in detectionResults) {
            // For hvert resultat fra modellen
            // Finder resultatets lokation, kategori og score
            val location = result.locationAsRectF;
            val category = result.categoryAsString;
            val score = result.scoreAsFloat;
            if (score < 0.88) {
                // Hvis scoren er for lav, så spring over
                continue
            }
            println("Score: $score")  // Udskriver scoren i loggen
            // Finder farven til firkanten, afhængig af kategorien
            paint.color = getRectColor(category)
            // Sætter paint objektet til at tegne en firkant
            paint.style = Paint.Style.STROKE
            // Tegner en firkant
            canvas.drawRect(
                RectF(
                    location.left * wScale,
                    location.top * hScale,
                    location.right * wScale,
                    location.bottom * hScale
                ), paint
            )
        }
        imageView.setImageBitmap(mutable)  // Viser billedet i ImageView
    }
    private fun getRectColor(category: String): Int {
        // Finder farven til firkanten, afhængig af kategorien
        val color: Int = when (category){
            "Hjul" -> R.color.sea  // Hvis kategorien er "Hjul", så bliver farven "sea"
            else -> R.color.white  // Ellers bliver farven "white"
        }
        return ContextCompat.getColor(this, color)  // Returnerer farven
    }
}