package com.zymat.scaffoldapp

import android.annotation.SuppressLint
import android.content.AsyncQueryHandler
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.Surface
import android.view.TextureView
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

    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var imageView: ImageView
    lateinit var button: Button
    lateinit var bitmap: Bitmap
    lateinit var model: ScaffoldModel
    lateinit var labels: List<String>

    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest

    private val paint = Paint()
    private val imageProcessor: ImageProcessor =
        ImageProcessor.Builder().add(
            ResizeOp(
                300,
                300,
                ResizeOp.ResizeMethod.BILINEAR
            )
        ).build()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPermissions()

        textureView = findViewById(R.id.imageV)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)

        textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                p0: SurfaceTexture,
                p1: Int,
                p2: Int
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                p0: SurfaceTexture,
                p1: Int,
                p2: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }
        }



        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)

        labels = FileUtil.loadLabels(this, "labels.txt")
        model = ScaffoldModel.newInstance(this)
        imageView = findViewById(R.id.imageV)
        button = findViewById(R.id.btn)

        button.setOnClickListener {
            startActivityForResult(intent, 101)
        }
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0], object: CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                var surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession.setRepeatingRequest(capReq.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        TODO("Not yet implemented")
                    }

                }, handler)
            }

            override fun onDisconnected(camera: CameraDevice) {

            }

            override fun onError(camera: CameraDevice, error: Int) {

            }
        }, handler)
    }

    fun getPermissions(){
        var permissionList = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.CAMERA)
        }
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionList.size > 0){
            requestPermissions(permissionList.toTypedArray(),101)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED){
                getPermissions()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            var uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
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