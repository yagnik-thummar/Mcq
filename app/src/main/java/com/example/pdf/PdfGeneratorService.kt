package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.GeneratedPaperEntity
import com.example.data.QuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfGeneratorService(private val context: Context) {

    // Standard A4 dimensions in points (72 points = 1 inch)
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 36f // 0.5 inch margin

    suspend fun generateQuestionPaperPdf(
        paper: GeneratedPaperEntity,
        questions: List<QuestionEntity>
    ): String = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val papersDir = File(context.filesDir, "papers").apply { if (!exists()) mkdirs() }
        val outputFile = File(papersDir, "QuestionPaper_${paper.id}_${System.currentTimeMillis()}.pdf")

        val isTwoColumn = paper.layoutMode.equals("TWO_COLUMN", ignoreCase = true)
        var pageNumber = 1

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdfDocument.startPage(currentPageInfo)
        var canvas = currentPage.canvas

        // Draw First Page Header
        var currentY = drawHeader(canvas, paper, headerPaint, subHeaderPaint, boldPaint, textPaint, linePaint)
        currentY = drawInstructions(canvas, paper.instructions, boldPaint, textPaint, linePaint, currentY)

        if (isTwoColumn) {
            val colWidth = (pageWidth - (margin * 2) - 16f) / 2f
            val leftColX = margin
            val rightColX = margin + colWidth + 16f
            val dividerX = margin + colWidth + 8f

            var activeCol = 0 // 0 = left, 1 = right
            var colY = currentY + 12f
            val startColY = colY
            var maxColYOnPage = startColY

            for ((index, q) in questions.withIndex()) {
                val qNumber = index + 1
                val colX = if (activeCol == 0) leftColX else rightColX
                val qHeight = estimateQuestionHeight(q, colWidth, textPaint, boldPaint)

                // Check if question fits in current column
                if (colY + qHeight > pageHeight - margin - 20f) {
                    if (activeCol == 0) {
                        // Switch to right column on same page
                        activeCol = 1
                        maxColYOnPage = maxOf(maxColYOnPage, colY)
                        colY = startColY
                    } else {
                        // Finish current page and start a new page
                        drawPageFooter(canvas, pageNumber, textPaint, linePaint)
                        // Draw vertical separator rule
                        canvas.drawLine(dividerX, startColY - 4f, dividerX, pageHeight - margin - 20f, linePaint)
                        pdfDocument.finishPage(currentPage)

                        pageNumber++
                        currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        currentPage = pdfDocument.startPage(currentPageInfo)
                        canvas = currentPage.canvas

                        drawRunningHeader(canvas, paper.title, textPaint, linePaint)
                        activeCol = 0
                        colY = margin + 25f
                        maxColYOnPage = colY
                    }
                }

                val targetX = if (activeCol == 0) leftColX else rightColX
                colY = drawQuestionBlock(canvas, q, qNumber, targetX, colY, colWidth, textPaint, boldPaint, linePaint)
                maxColYOnPage = maxOf(maxColYOnPage, colY)
            }

            // Draw final vertical divider
            canvas.drawLine(dividerX, startColY - 4f, dividerX, maxOf(maxColYOnPage, colY) + 10f, linePaint)
            drawPageFooter(canvas, pageNumber, textPaint, linePaint, isLastPage = true)
            pdfDocument.finishPage(currentPage)

        } else {
            // One Column Layout
            val contentWidth = pageWidth - (margin * 2)
            var yPos = currentY + 14f

            for ((index, q) in questions.withIndex()) {
                val qNumber = index + 1
                val qHeight = estimateQuestionHeight(q, contentWidth, textPaint, boldPaint)

                if (yPos + qHeight > pageHeight - margin - 20f) {
                    drawPageFooter(canvas, pageNumber, textPaint, linePaint)
                    pdfDocument.finishPage(currentPage)

                    pageNumber++
                    currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    currentPage = pdfDocument.startPage(currentPageInfo)
                    canvas = currentPage.canvas

                    drawRunningHeader(canvas, paper.title, textPaint, linePaint)
                    yPos = margin + 25f
                }

                yPos = drawQuestionBlock(canvas, q, qNumber, margin, yPos, contentWidth, textPaint, boldPaint, linePaint)
            }

            drawPageFooter(canvas, pageNumber, textPaint, linePaint, isLastPage = true)
            pdfDocument.finishPage(currentPage)
        }

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return@withContext outputFile.absolutePath
    }

    suspend fun generateSolutionKeyPdf(
        paper: GeneratedPaperEntity,
        questions: List<QuestionEntity>
    ): String = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val papersDir = File(context.filesDir, "papers").apply { if (!exists()) mkdirs() }
        val outputFile = File(papersDir, "SolutionKey_${paper.id}_${System.currentTimeMillis()}.pdf")

        var pageNumber = 1
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdfDocument.startPage(currentPageInfo)
        var canvas = currentPage.canvas

        // Header
        var yPos = margin + 16f
        canvas.drawText(paper.instituteName.uppercase(), (pageWidth / 2).toFloat(), yPos, headerPaint)
        yPos += 16f
        canvas.drawText("OFFICIAL ANSWER KEY & STEP-BY-STEP SOLUTIONS", (pageWidth / 2).toFloat(), yPos, headerPaint.apply { textSize = 11f })
        yPos += 14f
        canvas.drawText("Exam: ${paper.title}  |  Code: ${paper.subjectCode}  |  Total Questions: ${questions.size}", (pageWidth / 2).toFloat(), yPos, textPaint.apply { textAlign = Paint.Align.CENTER })
        textPaint.textAlign = Paint.Align.LEFT
        yPos += 10f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 14f

        // Quick Answer Key Table Matrix
        canvas.drawText("■ QUICK ANSWER KEY MATRIX", margin, yPos, boldPaint.apply { textSize = 10f })
        yPos += 12f

        val tableWidth = pageWidth - (margin * 2)
        val colsPerRow = 10
        val cellW = tableWidth / colsPerRow
        val cellH = 18f

        val totalRows = (questions.size + colsPerRow - 1) / colsPerRow
        for (r in 0 until totalRows) {
            val rowY = yPos + (r * cellH * 2)

            // Header row (Q1, Q2, ...)
            canvas.drawRect(margin, rowY, margin + tableWidth, rowY + cellH, linePaint)
            // Answer row (A, B, ...)
            canvas.drawRect(margin, rowY + cellH, margin + tableWidth, rowY + (cellH * 2), linePaint)

            for (c in 0 until colsPerRow) {
                val qIndex = (r * colsPerRow) + c
                val cellX = margin + (c * cellW)
                canvas.drawLine(cellX, rowY, cellX, rowY + (cellH * 2), linePaint)

                if (qIndex < questions.size) {
                    val q = questions[qIndex]
                    val qNum = "Q${qIndex + 1}"
                    canvas.drawText(qNum, cellX + 6f, rowY + 12f, boldPaint.apply { textSize = 8.5f })
                    canvas.drawText(q.correctOption, cellX + (cellW / 2) - 4f, rowY + cellH + 13f, boldPaint.apply { textSize = 9.5f; color = Color.parseColor("#047857") })
                    boldPaint.color = Color.BLACK
                }
            }
        }

        yPos += (totalRows * cellH * 2) + 20f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 16f

        // Detailed Solutions Section
        canvas.drawText("■ STEP-BY-STEP DETAILED SOLUTIONS", margin, yPos, boldPaint.apply { textSize = 11f })
        yPos += 14f

        val contentWidth = pageWidth - (margin * 2)
        for ((index, q) in questions.withIndex()) {
            val qNum = index + 1
            val cleanSolution = q.solutionText.ifBlank { "Direct conceptual application. Correct Answer: Option (${q.correctOption})" }
            val solLines = wrapText("Explanation: $cleanSolution", contentWidth - 16f, textPaint)
            val estHeight = (solLines.size * 14f) + 36f

            if (yPos + estHeight > pageHeight - margin - 20f) {
                drawPageFooter(canvas, pageNumber, textPaint, linePaint)
                pdfDocument.finishPage(currentPage)

                pageNumber++
                currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(currentPageInfo)
                canvas = currentPage.canvas

                drawRunningHeader(canvas, "${paper.title} - Solutions", textPaint, linePaint)
                yPos = margin + 25f
            }

            // Draw Solution Card
            canvas.drawRect(margin, yPos, margin + contentWidth, yPos + estHeight, linePaint.apply { color = Color.LTGRAY })
            linePaint.color = Color.BLACK

            canvas.drawText("Q$qNum.  Correct Option: [ ${q.correctOption} ]", margin + 8f, yPos + 14f, boldPaint.apply { textSize = 10f; color = Color.parseColor("#1E1B4B") })
            boldPaint.color = Color.BLACK

            var lineY = yPos + 28f
            for (line in solLines) {
                canvas.drawText(cleanLatexSymbolsForPdf(line), margin + 8f, lineY, textPaint.apply { textSize = 9f })
                lineY += 13f
            }

            yPos += estHeight + 10f
        }

        drawPageFooter(canvas, pageNumber, textPaint, linePaint, isLastPage = true)
        pdfDocument.finishPage(currentPage)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return@withContext outputFile.absolutePath
    }

    private fun drawHeader(
        canvas: Canvas,
        paper: GeneratedPaperEntity,
        headerPaint: Paint,
        subHeaderPaint: Paint,
        boldPaint: Paint,
        textPaint: Paint,
        linePaint: Paint
    ): Float {
        var y = margin + 14f
        val centerX = (pageWidth / 2).toFloat()

        // Institute Name
        headerPaint.textSize = 14f
        canvas.drawText(paper.instituteName.uppercase(), centerX, y, headerPaint)
        y += 14f

        // Subtitle / Department
        subHeaderPaint.textSize = 9f
        canvas.drawText(paper.instituteSubtitle, centerX, y, subHeaderPaint)
        y += 14f

        // Exam Title
        headerPaint.textSize = 12f
        canvas.drawText(paper.title, centerX, y, headerPaint)
        y += 14f

        // Metadata box
        val boxTop = y
        val boxWidth = pageWidth - (margin * 2)
        val boxHeight = 36f

        canvas.drawRect(margin, boxTop, margin + boxWidth, boxTop + boxHeight, linePaint)
        canvas.drawLine(margin, boxTop + 18f, margin + boxWidth, boxTop + 18f, linePaint)
        canvas.drawLine(margin + (boxWidth / 3), boxTop, margin + (boxWidth / 3), boxTop + boxHeight, linePaint)
        canvas.drawLine(margin + (2 * boxWidth / 3), boxTop, margin + (2 * boxWidth / 3), boxTop + boxHeight, linePaint)

        // Row 1
        canvas.drawText("Subject: ${paper.subjectCode}", margin + 6f, boxTop + 13f, textPaint.apply { textSize = 8.5f })
        canvas.drawText("Class: ${paper.gradeClass}", margin + (boxWidth / 3) + 6f, boxTop + 13f, textPaint)
        canvas.drawText("Date: ${paper.examDate}", margin + (2 * boxWidth / 3) + 6f, boxTop + 13f, textPaint)

        // Row 2
        canvas.drawText("Time: ${paper.durationMinutes} Mins", margin + 6f, boxTop + 30f, textPaint)
        canvas.drawText("Max Marks: ${paper.totalMarks}", margin + (boxWidth / 3) + 6f, boxTop + 30f, textPaint)
        canvas.drawText("Marking: ${paper.negativeMarkingText}", margin + (2 * boxWidth / 3) + 6f, boxTop + 30f, textPaint)

        return boxTop + boxHeight + 10f
    }

    private fun drawInstructions(
        canvas: Canvas,
        instructions: String,
        boldPaint: Paint,
        textPaint: Paint,
        linePaint: Paint,
        startY: Float
    ): Float {
        val width = pageWidth - (margin * 2)
        val lines = wrapText(instructions, width - 16f, textPaint.apply { textSize = 8.5f })
        val height = 18f + (lines.size * 11f)

        canvas.drawRect(margin, startY, margin + width, startY + height, linePaint.apply { color = Color.DKGRAY })
        linePaint.color = Color.BLACK

        canvas.drawText("GENERAL INSTRUCTIONS:", margin + 8f, startY + 12f, boldPaint.apply { textSize = 8.5f })
        var currentY = startY + 23f
        for (line in lines) {
            canvas.drawText(line, margin + 8f, currentY, textPaint.apply { textSize = 8f })
            currentY += 11f
        }

        return startY + height + 6f
    }

    private fun drawQuestionBlock(
        canvas: Canvas,
        q: QuestionEntity,
        qNumber: Int,
        x: Float,
        y: Float,
        width: Float,
        textPaint: Paint,
        boldPaint: Paint,
        linePaint: Paint
    ): Float {
        var curY = y + 10f

        // Q Number and Marks
        val qHeader = "Q$qNumber. "
        canvas.drawText(qHeader, x, curY, boldPaint.apply { textSize = 9.5f })
        val headerOffset = boldPaint.measureText(qHeader)

        val marksText = "[${q.marks}M]"
        val marksWidth = boldPaint.measureText(marksText)
        canvas.drawText(marksText, x + width - marksWidth, curY, boldPaint.apply { textSize = 8.5f; color = Color.DKGRAY })
        boldPaint.color = Color.BLACK

        // Question Text lines
        val cleanedQText = cleanLatexSymbolsForPdf(q.questionText)
        val qLines = wrapText(cleanedQText, width - headerOffset - marksWidth - 6f, textPaint.apply { textSize = 9f })

        for ((i, line) in qLines.withIndex()) {
            if (i == 0) {
                canvas.drawText(line, x + headerOffset, curY, textPaint)
            } else {
                curY += 12f
                canvas.drawText(line, x + 6f, curY, textPaint)
            }
        }
        curY += 14f

        // Diagram (if present)
        if (!q.diagramPath.isNullOrBlank()) {
            val diagramFile = File(q.diagramPath)
            if (diagramFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(diagramFile.absolutePath)
                if (bitmap != null) {
                    val maxDiagH = 90f
                    val diagW = minOf(width - 16f, (bitmap.width.toFloat() / bitmap.height.toFloat()) * maxDiagH)
                    val diagH = (bitmap.height.toFloat() / bitmap.width.toFloat()) * diagW
                    val diagX = x + (width - diagW) / 2f
                    val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                    val dstRect = RectF(diagX, curY, diagX + diagW, curY + diagH)
                    canvas.drawBitmap(bitmap, srcRect, dstRect, null)
                    curY += diagH + 10f
                }
            }
        }

        // Options A, B, C, D
        val optA = "(A) " + cleanLatexSymbolsForPdf(q.optionA)
        val optB = "(B) " + cleanLatexSymbolsForPdf(q.optionB)
        val optC = "(C) " + cleanLatexSymbolsForPdf(q.optionC)
        val optD = "(D) " + cleanLatexSymbolsForPdf(q.optionD)

        val halfW = width / 2f
        val isShort = optA.length < 22 && optB.length < 22 && optC.length < 22 && optD.length < 22

        if (isShort) {
            // 2x2 grid for options
            canvas.drawText(optA, x + 8f, curY, textPaint.apply { textSize = 8.5f })
            canvas.drawText(optB, x + halfW + 4f, curY, textPaint)
            curY += 12f
            canvas.drawText(optC, x + 8f, curY, textPaint)
            canvas.drawText(optD, x + halfW + 4f, curY, textPaint)
            curY += 14f
        } else {
            // Stacked options
            for (opt in listOf(optA, optB, optC, optD)) {
                val optLines = wrapText(opt, width - 12f, textPaint.apply { textSize = 8.5f })
                for (line in optLines) {
                    canvas.drawText(line, x + 8f, curY, textPaint)
                    curY += 12f
                }
            }
            curY += 4f
        }

        // Subtle separator line
        canvas.drawLine(x, curY + 2f, x + width, curY + 2f, linePaint.apply { color = Color.parseColor("#E2E8F0") })
        linePaint.color = Color.BLACK

        return curY + 6f
    }

    private fun estimateQuestionHeight(
        q: QuestionEntity,
        width: Float,
        textPaint: Paint,
        boldPaint: Paint
    ): Float {
        var h = 30f // base question spacing
        val cleanedQText = cleanLatexSymbolsForPdf(q.questionText)
        val qLines = wrapText(cleanedQText, width - 40f, textPaint)
        h += qLines.size * 12f

        if (!q.diagramPath.isNullOrBlank() && File(q.diagramPath).exists()) {
            h += 95f
        }

        val optA = "(A) " + cleanLatexSymbolsForPdf(q.optionA)
        val optB = "(B) " + cleanLatexSymbolsForPdf(q.optionB)
        val isShort = optA.length < 22 && optB.length < 22
        h += if (isShort) 28f else 52f

        return h
    }

    private fun drawRunningHeader(canvas: Canvas, title: String, textPaint: Paint, linePaint: Paint) {
        val y = margin + 10f
        canvas.drawText(title, margin, y, textPaint.apply { textSize = 8.5f; color = Color.DKGRAY })
        canvas.drawLine(margin, y + 5f, pageWidth - margin, y + 5f, linePaint.apply { color = Color.LTGRAY })
        linePaint.color = Color.BLACK
        textPaint.color = Color.BLACK
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, textPaint: Paint, linePaint: Paint, isLastPage: Boolean = false) {
        val y = pageHeight - margin + 10f
        canvas.drawLine(margin, y - 10f, pageWidth - margin, y - 10f, linePaint.apply { color = Color.LTGRAY })
        linePaint.color = Color.BLACK

        val pageStr = "Page $pageNumber"
        canvas.drawText(pageStr, (pageWidth / 2) - 15f, y, textPaint.apply { textSize = 8.5f; color = Color.DKGRAY })

        if (isLastPage) {
            val endStr = "*** End of Question Paper ***"
            canvas.drawText(endStr, margin, y, textPaint.apply { textSize = 8f; color = Color.GRAY })
        }
        textPaint.color = Color.BLACK
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (para in paragraphs) {
            val words = para.split(" ")
            var currentLine = ""

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val measuredW = paint.measureText(testLine)
                if (measuredW <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine)
                    }
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine)
            }
        }
        return result
    }

    private fun cleanLatexSymbolsForPdf(raw: String): String {
        return raw
            .replace("$$\\int", "∫ ")
            .replace("\\int", "∫")
            .replace("\\frac", "")
            .replace("\\sqrt", "√")
            .replace("\\lim", "lim")
            .replace("\\sum", "∑")
            .replace("\\infty", "∞")
            .replace("\\rightarrow", " → ")
            .replace("\\xrightarrow", " ──→ ")
            .replace("\\rightleftharpoons", " ⇌ ")
            .replace("\\Delta", "Δ")
            .replace("\\alpha", "α")
            .replace("\\beta", "β")
            .replace("\\gamma", "γ")
            .replace("\\theta", "θ")
            .replace("\\pi", "π")
            .replace("\\lambda", "λ")
            .replace("\\pm", "±")
            .replace("\\times", "×")
            .replace("\\div", "÷")
            .replace("\\le", "≤")
            .replace("\\ge", "≥")
            .replace("\\ne", "≠")
            .replace("\\approx", "≈")
            .replace("\\cdot", "·")
            .replace("\\quad", " ")
            .replace("\\text", "")
            .replace("\\ce", "")
            .replace("$$", "")
            .replace("$", "")
            .replace("{", "")
            .replace("}", "")
            .replace("\\", "")
            .trim()
    }
}
