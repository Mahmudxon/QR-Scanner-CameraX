package uz.mahmudxon.scanner

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ScannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val executor = Executors.newSingleThreadExecutor()
    private var imageRotationDegrees: Int = 0
    private var filter: ScannerFilter? = null

    fun setFilter(filter: ScannerFilter) {
        this.filter = filter
    }

    var listener: ((List<String>) -> Unit)? = null

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    var isFlashlightOn = false
        private set

    private val options by lazy {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    private lateinit var viewFinder: PreviewView
    private lateinit var graphicOverlayFinder: GraphicOverlay

    private lateinit var camera: Camera
    private val scanner by lazy {
        BarcodeScannerOptions.Builder().build().let { BarcodeScanning.getClient(options) }
    }

    private lateinit var bitmapBuffer: Bitmap
    private var pauseAnalysis = false

    init {
        initializeViews()
    }

    fun turnOnTorch() = setTorchMode(true)
    fun turnOffTorch() = setTorchMode(false)

    private fun setTorchMode(value: Boolean) {
        if (!isTorchEnable())
            return
        isFlashlightOn = value
        camera.cameraControl.enableTorch(value)
    }

    fun isTorchEnable(): Boolean = camera.cameraInfo.hasFlashUnit()

    fun switchCamera() {
        if (isFlashlightOn) turnOffTorch()
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        startCamera()
    }

    private fun initializeViews() {
        viewFinder = PreviewView(context)
        graphicOverlayFinder = GraphicOverlay(context, null)
        addView(viewFinder)
        addView(graphicOverlayFinder)
        val viewFinderLayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        viewFinder.layoutParams = viewFinderLayoutParams
        val graphicOverlayLayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        graphicOverlayFinder.layoutParams = graphicOverlayLayoutParams
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val resolutionSelector =
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .build()

            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .setTargetRotation(viewFinder.display.rotation)
                .build()


            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setTargetRotation(viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()


            imageAnalysis.setAnalyzer(executor, ImageAnalysis.Analyzer { image ->
                if (!::bitmapBuffer.isInitialized) {
                    imageRotationDegrees = image.imageInfo.rotationDegrees
                    bitmapBuffer = Bitmap.createBitmap(
                        image.width, image.height, Bitmap.Config.ARGB_8888
                    )
                }

                if (pauseAnalysis) {
                    image.close()
                    return@Analyzer
                }
                image.use {
                    bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)
                    scanner.process(bitmapBuffer, imageRotationDegrees)
                        .addOnSuccessListener { result ->
                            val barcodes = if (filter != null) result.filter {
                                filter?.isAvailable(it.rawValue ?: "") == true
                            } else result
                            graphicOverlayFinder.clear()
                            graphicOverlayFinder.add(
                                barcodes.map {
                                    QrContourGraphic(
                                        graphicOverlayFinder,
                                        it,
                                        image.cropRect
                                    )
                                })
                            graphicOverlayFinder.postInvalidate()
                            onQrResult(barcodes.map { it.rawValue ?: "" })
                        }
                }
            })

            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing)
                .build()

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, imageAnalysis
            )
            preview.setSurfaceProvider(viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(context))
    }

    fun startAnalyze() {
        pauseAnalysis = false
    }

    fun stopAnalyze() {
        pauseAnalysis = true
    }

    private fun onQrResult(barcodes: List<String>) {
        listener?.invoke(barcodes)
    }

    fun stopCamera() {
        executor.apply {
            shutdown()
            awaitTermination(1000, TimeUnit.MILLISECONDS)
        }
    }

}