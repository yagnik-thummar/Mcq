package com.example.pdf

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfPreviewManager(private val appContext: Context) {

    suspend fun renderPdfPagesToBitmaps(pdfFilePath: String): List<Bitmap> = withContext(Dispatchers.IO) {
        val file = File(pdfFilePath)
        if (!file.exists()) return@withContext emptyList()

        val bitmaps = mutableListOf<Bitmap>()
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            val pageCount = pdfRenderer.pageCount
            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                // 2x scale for crisp sharp rendering on modern mobile displays
                val scale = 2
                val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext bitmaps
    }

    fun sharePdf(context: Context, pdfFilePath: String, title: String = "Question Paper") {
        val file = File(pdfFilePath)
        if (!file.exists()) return

        val authority = "${context.packageName}.provider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Generated Exam Paper: $title\nCreated with MCQ Generator App.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Exam Paper PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun sharePdf(pdfFilePath: String, title: String = "Question Paper") {
        sharePdf(appContext, pdfFilePath, title)
    }

    fun printPdf(context: Context, pdfFilePath: String, jobName: String = "Exam_Question_Paper") {
        val file = File(pdfFilePath)
        if (!file.exists()) return

        val activity = findActivity(context) ?: findActivity(appContext)

        if (activity != null) {
            try {
                val printManager = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (printManager != null) {
                    val printAdapter = object : PrintDocumentAdapter() {
                        override fun onLayout(
                            oldAttributes: PrintAttributes?,
                            newAttributes: PrintAttributes?,
                            cancellationSignal: CancellationSignal?,
                            callback: LayoutResultCallback?,
                            extras: Bundle?
                        ) {
                            if (cancellationSignal?.isCanceled == true) {
                                callback?.onLayoutCancelled()
                                return
                            }
                            val info = PrintDocumentInfo.Builder("$jobName.pdf")
                                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                .build()
                            callback?.onLayoutFinished(info, true)
                        }

                        override fun onWrite(
                            pages: Array<out PageRange>?,
                            destination: ParcelFileDescriptor?,
                            cancellationSignal: CancellationSignal?,
                            callback: WriteResultCallback?
                        ) {
                            try {
                                FileInputStream(file).use { input ->
                                    FileOutputStream(destination?.fileDescriptor).use { output ->
                                        val buf = ByteArray(1024)
                                        var bytesRead: Int
                                        while (input.read(buf).also { bytesRead = it } > 0) {
                                            output.write(buf, 0, bytesRead)
                                        }
                                    }
                                }
                                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                            } catch (e: Exception) {
                                callback?.onWriteFailed(e.message)
                            }
                        }
                    }

                    printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback: Open with default system PDF viewer/print handler if direct activity print manager failed
        try {
            val authority = "${context.packageName}.provider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printPdf(pdfFilePath: String, jobName: String = "Exam_Question_Paper") {
        printPdf(appContext, pdfFilePath, jobName)
    }

    private fun findActivity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
