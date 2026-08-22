package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

@Composable
fun DiagramCropper(
    sourceBitmap: Bitmap,
    onQuestionCropped: (Bitmap) -> Unit,
    onDiagramExtracted: (String, Bitmap) -> Unit, // returns saved filePath and cropped Bitmap
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Crop box coordinates normalized (0.0f to 1.0f)
    var cropLeft by remember { mutableFloatStateOf(0.1f) }
    var cropTop by remember { mutableFloatStateOf(0.15f) }
    var cropRight by remember { mutableFloatStateOf(0.9f) }
    var cropBottom by remember { mutableFloatStateOf(0.85f) }

    var lastCroppedDiagramBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📐 Adjust Crop Box & Select Area",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Drag handles or box to isolate Question text or Diagram",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            val boxWidth = maxWidth.value
            val boxHeight = maxHeight.value

            Image(
                bitmap = sourceBitmap.asImageBitmap(),
                contentDescription = "Source Exam Sheet",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Interactive Crop Overlay Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaX = dragAmount.x / size.width
                            val deltaY = dragAmount.y / size.height

                            val currentLeft = cropLeft
                            val currentTop = cropTop
                            val currentRight = cropRight
                            val currentBottom = cropBottom

                            val touchX = change.position.x / size.width
                            val touchY = change.position.y / size.height

                            val handleRadius = 0.15f

                            // Check which handle or box is near
                            when {
                                Math.hypot((touchX - currentLeft).toDouble(), (touchY - currentTop).toDouble()) < handleRadius -> {
                                    cropLeft = (currentLeft + deltaX).coerceIn(0.0f, currentRight - 0.1f)
                                    cropTop = (currentTop + deltaY).coerceIn(0.0f, currentBottom - 0.1f)
                                }
                                Math.hypot((touchX - currentRight).toDouble(), (touchY - currentTop).toDouble()) < handleRadius -> {
                                    cropRight = (currentRight + deltaX).coerceIn(currentLeft + 0.1f, 1.0f)
                                    cropTop = (currentTop + deltaY).coerceIn(0.0f, currentBottom - 0.1f)
                                }
                                Math.hypot((touchX - currentLeft).toDouble(), (touchY - currentBottom).toDouble()) < handleRadius -> {
                                    cropLeft = (currentLeft + deltaX).coerceIn(0.0f, currentRight - 0.1f)
                                    cropBottom = (currentBottom + deltaY).coerceIn(currentTop + 0.1f, 1.0f)
                                }
                                Math.hypot((touchX - currentRight).toDouble(), (touchY - currentBottom).toDouble()) < handleRadius -> {
                                    cropRight = (currentRight + deltaX).coerceIn(currentLeft + 0.1f, 1.0f)
                                    cropBottom = (currentBottom + deltaY).coerceIn(currentTop + 0.1f, 1.0f)
                                }
                                else -> {
                                    // Move entire box
                                    val w = currentRight - currentLeft
                                    val h = currentBottom - currentTop
                                    var newLeft = currentLeft + deltaX
                                    var newTop = currentTop + deltaY
                                    if (newLeft < 0f) newLeft = 0f
                                    if (newTop < 0f) newTop = 0f
                                    if (newLeft + w > 1f) newLeft = 1f - w
                                    if (newTop + h > 1f) newTop = 1f - h
                                    cropLeft = newLeft
                                    cropTop = newTop
                                    cropRight = newLeft + w
                                    cropBottom = newTop + h
                                }
                            }
                        }
                    }
            ) {
                val leftPx = cropLeft * size.width
                val topPx = cropTop * size.height
                val rightPx = cropRight * size.width
                val bottomPx = cropBottom * size.height
                val widthPx = rightPx - leftPx
                val heightPx = bottomPx - topPx

                // Draw translucent scrim around crop box
                // Top
                drawRect(Color.Black.copy(alpha = 0.55f), topLeft = Offset(0f, 0f), size = Size(size.width, topPx))
                // Bottom
                drawRect(Color.Black.copy(alpha = 0.55f), topLeft = Offset(0f, bottomPx), size = Size(size.width, size.height - bottomPx))
                // Left
                drawRect(Color.Black.copy(alpha = 0.55f), topLeft = Offset(0f, topPx), size = Size(leftPx, heightPx))
                // Right
                drawRect(Color.Black.copy(alpha = 0.55f), topLeft = Offset(rightPx, topPx), size = Size(size.width - rightPx, heightPx))

                // Crop box border
                drawRect(
                    color = Color(0xFF6366F1),
                    topLeft = Offset(leftPx, topPx),
                    size = Size(widthPx, heightPx),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Grid lines inside crop box
                val oneThirdW = widthPx / 3f
                val oneThirdH = heightPx / 3f
                drawLine(Color.White.copy(alpha = 0.4f), Offset(leftPx + oneThirdW, topPx), Offset(leftPx + oneThirdW, bottomPx), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.4f), Offset(leftPx + 2 * oneThirdW, topPx), Offset(leftPx + 2 * oneThirdW, bottomPx), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.4f), Offset(leftPx, topPx + oneThirdH), Offset(rightPx, topPx + oneThirdH), strokeWidth = 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.4f), Offset(leftPx, topPx + 2 * oneThirdH), Offset(rightPx, topPx + 2 * oneThirdH), strokeWidth = 1.dp.toPx())

                // 4 Corner circle handles
                val handleRadiusPx = 10.dp.toPx()
                drawCircle(Color.White, radius = handleRadiusPx, center = Offset(leftPx, topPx))
                drawCircle(Color(0xFF6366F1), radius = handleRadiusPx - 2.dp.toPx(), center = Offset(leftPx, topPx))

                drawCircle(Color.White, radius = handleRadiusPx, center = Offset(rightPx, topPx))
                drawCircle(Color(0xFF6366F1), radius = handleRadiusPx - 2.dp.toPx(), center = Offset(rightPx, topPx))

                drawCircle(Color.White, radius = handleRadiusPx, center = Offset(leftPx, bottomPx))
                drawCircle(Color(0xFF6366F1), radius = handleRadiusPx - 2.dp.toPx(), center = Offset(leftPx, bottomPx))

                drawCircle(Color.White, radius = handleRadiusPx, center = Offset(rightPx, bottomPx))
                drawCircle(Color(0xFF6366F1), radius = handleRadiusPx - 2.dp.toPx(), center = Offset(rightPx, bottomPx))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val cropped = getCroppedBitmap(sourceBitmap, cropLeft, cropTop, cropRight, cropBottom)
                    onQuestionCropped(cropped)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crop for OCR", fontSize = 13.sp)
            }

            FilledTonalButton(
                onClick = {
                    val cropped = getCroppedBitmap(sourceBitmap, cropLeft, cropTop, cropRight, cropBottom)
                    val savedPath = saveDiagramToInternalStorage(context, cropped)
                    lastCroppedDiagramBitmap = cropped
                    onDiagramExtracted(savedPath, cropped)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Diagram", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = {
                    cropLeft = 0.05f
                    cropTop = 0.05f
                    cropRight = 0.95f
                    cropBottom = 0.95f
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Crop")
            }
        }

        if (lastCroppedDiagramBitmap != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = lastCroppedDiagramBitmap!!.asImageBitmap(),
                    contentDescription = "Saved diagram",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "✓ Diagram extracted & linked to question!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF047857)
                )
            }
        }
    }
}

fun getCroppedBitmap(source: Bitmap, leftNorm: Float, topNorm: Float, rightNorm: Float, bottomNorm: Float): Bitmap {
    val srcWidth = source.width
    val srcHeight = source.height

    val x = (leftNorm * srcWidth).toInt().coerceIn(0, srcWidth - 1)
    val y = (topNorm * srcHeight).toInt().coerceIn(0, srcHeight - 1)
    val width = ((rightNorm - leftNorm) * srcWidth).toInt().coerceIn(1, srcWidth - x)
    val height = ((bottomNorm - topNorm) * srcHeight).toInt().coerceIn(1, srcHeight - y)

    return Bitmap.createBitmap(source, x, y, width, height)
}

fun saveDiagramToInternalStorage(context: Context, bitmap: Bitmap): String {
    val diagramsDir = File(context.filesDir, "diagrams")
    if (!diagramsDir.exists()) {
        diagramsDir.mkdirs()
    }
    val fileName = "diagram_${System.currentTimeMillis()}.png"
    val file = File(diagramsDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file.absolutePath
}

object DemoExamSheets {
    fun createMathSampleSheet(): Bitmap {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.parseColor("#FAF8F5"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#1E293B")
            textSize = 28f
            isFakeBoldText = true
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#475569")
            textSize = 22f
        }

        val mathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#312E81")
            textSize = 26f
            isFakeBoldText = true
        }

        canvas.drawText("MATHEMATICS - SECTION A (CALCULUS)", 50f, 60f, paint)
        canvas.drawLine(50f, 75f, 750f, 75f, paint.apply { strokeWidth = 2f })

        canvas.drawText("Q1. Evaluate the definite integral:", 50f, 130f, paint)
        canvas.drawText("     ∫ [0 to π/2] (√sin x) / (√sin x + √cos x) dx", 50f, 180f, mathPaint)

        canvas.drawText("(A)  π / 4", 80f, 250f, subPaint)
        canvas.drawText("(B)  π / 2", 400f, 250f, subPaint)
        canvas.drawText("(C)  π", 80f, 310f, subPaint)
        canvas.drawText("(D)  0", 400f, 310f, subPaint)

        // Draw geometric integral curve diagram box
        val diagramPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#4338CA")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(50f, 370f, 750f, 550f, diagramPaint.apply { color = AndroidColor.parseColor("#CBD5E1") })
        canvas.drawText("DIAGRAM AREA (Symmetric Curve y = f(x) over [0, π/2])", 70f, 400f, subPaint)
        
        // Draw coordinate axes
        canvas.drawLine(100f, 520f, 700f, 520f, diagramPaint.apply { color = AndroidColor.parseColor("#64748B") }) // X-axis
        canvas.drawLine(150f, 530f, 150f, 420f, diagramPaint) // Y-axis
        
        // Draw curve
        val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#6366F1")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        val path = android.graphics.Path().apply {
            moveTo(150f, 500f)
            quadTo(350f, 430f, 650f, 440f)
        }
        canvas.drawPath(path, curvePaint)
        canvas.drawText("Area = π/4", 360f, 480f, mathPaint.apply { textSize = 20f })

        return bitmap
    }

    fun createChemSampleSheet(): Bitmap {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.parseColor("#F8FAFC"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#0F172A")
            textSize = 28f
            isFakeBoldText = true
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#334155")
            textSize = 22f
        }

        val chemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#0D9488")
            textSize = 26f
            isFakeBoldText = true
        }

        canvas.drawText("CHEMISTRY - ORGANIC REACTION SCHEME", 50f, 60f, paint)
        canvas.drawLine(50f, 75f, 750f, 75f, paint.apply { strokeWidth = 2f })

        canvas.drawText("Q. Identify the major product [X] in the following reaction:", 50f, 130f, paint)
        canvas.drawText("    CH3CH2OH  +  conc. H2SO4 (443 K)  ──→  [X] + H2O", 50f, 180f, chemPaint)

        canvas.drawText("(A)  CH2 = CH2 (Ethene)", 80f, 250f, subPaint)
        canvas.drawText("(B)  CH3CH2-O-CH2CH3 (Diethyl ether)", 400f, 250f, subPaint)
        canvas.drawText("(C)  CH3CHO (Ethanal)", 80f, 310f, subPaint)
        canvas.drawText("(D)  CH3COOH (Ethanoic Acid)", 400f, 310f, subPaint)

        // Draw Benzene Ring / Mechanism Diagram Box
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#CBD5E1")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(50f, 360f, 750f, 560f, boxPaint)
        canvas.drawText("REACTION MECHANISM DIAGRAM (Intramolecular Dehydration)", 70f, 395f, subPaint)

        // Draw Benzene / molecule hexagon
        val moleculePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.parseColor("#0F766E")
            strokeWidth = 3.5f
            style = Paint.Style.STROKE
        }
        val hexPath = android.graphics.Path().apply {
            moveTo(250f, 440f)
            lineTo(290f, 420f)
            lineTo(330f, 440f)
            lineTo(330f, 490f)
            lineTo(290f, 510f)
            lineTo(250f, 490f)
            close()
        }
        canvas.drawPath(hexPath, moleculePaint)
        canvas.drawCircle(290f, 465f, 20f, moleculePaint.apply { strokeWidth = 2f })
        canvas.drawText("C6H5 — OH  +  Br2  ──→  2,4,6-Tribromophenol", 360f, 470f, chemPaint.apply { textSize = 20f })

        return bitmap
    }
}
