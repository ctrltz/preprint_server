package preprint.server.pdf

import preprint.server.data.Data
import preprint.server.references.PDFBoldTextStripper
import preprint.server.references.ReferenceExtractor

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result;
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.IOException
import java.lang.Thread.sleep

object PdfHandler {
    private const val SLEEP_TIME : Long = 2000
    fun getFullInfo(recordList : List <Data>, outputPath : String) {
        for ((i, record) in recordList.withIndex()) {
            println("downloading $i: ${record.id}")
            println(record.pdfUrl)

            if (record.pdfUrl == "") {
                File(outputPath + "failed.txt").appendText("${record.id}\n")
                continue
            }

            val pdf = downloadPdf(record.pdfUrl) ?: return
            File("$outputPath${record.id}.pdf").writeBytes(pdf)

            val (pdfText, pageWidth) =  try {
                parsePdf(pdf)
            } catch (e : IOException) {
                println(e.message)
                File(outputPath + "failed.txt").appendText("${record.id}\n")
                continue
            }

            record.refList = ReferenceExtractor.extract(pdfText, pageWidth).toMutableList()
            sleep(SLEEP_TIME)
        }
    }

    fun downloadPdf(url : String) : ByteArray? {
        val (_, _, result) = url
            .httpGet()
            .response()
        return when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                println(ex)
                null
            }
            is Result.Success -> {
                println("Success")
                result.get()
            }
        }
    }

    fun parsePdf(pdf : ByteArray) : Pair<String, Double> {
        val pdfStripper = PDFBoldTextStripper()
        val doc = PDDocument.load(pdf)
        val pageWidth = doc.pages[0].mediaBox.width.toDouble()
        val text = pdfStripper.getText(doc)
        doc.close()
        return Pair(text, pageWidth)
    }
}