package uz.mahmudxon.scanner

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode

class QrContourGraphic(
    overlay: GraphicOverlay,
    private val barcode: Barcode,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint

    init {
        val selectedColor = Color.YELLOW

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }

    override fun draw(canvas: Canvas?) {
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            barcode.boundingBox ?: Rect()
        )
        if (canvas == null) return

        // Calculate corner length dynamically based on the size of the QR code rectangle
        val cornerLength = minOf(rect.width(), rect.height()) * 0.1f

        // Draw the corners of the QR code
        // Top-left corner
        canvas.drawLine(rect.left, rect.top, rect.left + cornerLength, rect.top, boxPaint)
        canvas.drawLine(rect.left, rect.top, rect.left, rect.top - cornerLength, boxPaint)

        // Top-right corner
        canvas.drawLine(rect.right, rect.top, rect.right - cornerLength, rect.top, boxPaint)
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top - cornerLength, boxPaint)

        // Bottom-left corner
        canvas.drawLine(rect.left, rect.bottom, rect.left + cornerLength, rect.bottom, boxPaint)
        canvas.drawLine(rect.left, rect.bottom + cornerLength, rect.left, rect.bottom, boxPaint)

        // Bottom-right corner
        canvas.drawLine(rect.right, rect.bottom, rect.right - cornerLength, rect.bottom, boxPaint)
        canvas.drawLine(rect.right, rect.bottom + cornerLength, rect.right, rect.bottom, boxPaint)
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}