package com.dika.myaksoro

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import org.pytorch.LiteModuleLoader

// Tambahkan parameter Context di constructor
class AksoroEngine(private val context: Context) {

    private var cnnModule: Module? = null
    private var seq2seqModule: Module? = null
    private val MAX_INPUT_LEN = 15
    private val MAX_OUTPUT_LEN = 2

    // Hyperparameter sesuai skrip Python
    private val CONFIDENCE_THRESHOLD = 0.25f
    private val CNN_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val CNN_STD = floatArrayOf(0.229f, 0.224f, 0.225f)

    // CATATAN PENTING: Urutan kelas ini HARUS persis sama dengan urutan saat kamu melatih model di Python
    private val classNames = arrayOf(
        "a", "ba", "ca", "cecak", "da", "dha", "e", "ga", "ha", "i",
        "ja", "ka", "la", "layar", "ma", "na", "nga", "nya", "o",
        "p_ba", "p_ca", "p_da", "p_dha", "p_ga", "p_ha", "p_ja", "p_ka",
        "p_la", "p_ma", "p_nga", "p_pa", "p_ra", "p_sa", "p_ta", "p_tha", "p_ya",
        "pa", "pangkon", "pepet", "ra", "sa", "ta", "taling", "tarung",
        "tha", "u", "wa", "wignyan", "wulu", "ya"
    )

    // Vocab Seq2Seq
    private val inputTokens = listOf(
        "<pad>", "<sos>", "<eos>",
        "a", "ba", "ca", "cecak", "da", "dha", "e", "ga", "ha", "i",
        "ja", "ka", "la", "layar", "ma", "na", "nga", "nya", "o", "pa",
        "pangkon", "pepet", "p_ba", "p_ca", "p_da", "p_dha", "p_ga", "p_ha", "p_ja", "p_ka",
        "p_la", "p_ma", "p_nga", "p_pa", "p_ra", "p_sa", "p_ta", "p_tha", "p_ya", "ra",
        "sa", "ta", "taling", "tarung", "tha", "u", "wa", "wignyan", "wulu", "ya"
    )
    private val inputVocab = inputTokens.withIndex().associate { it.value to it.index }
    private val outputTokens = listOf("<pad>", "<sos>", "<eos>") + "abcdefghijklmnopqrstuvwxyzê ".map { it.toString() }
    private val outputVocab = outputTokens.withIndex().associate { it.value to it.index }
    private val invOutputVocab = outputTokens.mapIndexed { index, s -> index to s }.toMap()

    // Kumpulan Aturan Penggalan Kata (Chunking Rules)
    private val nglegena = setOf("ha", "na", "ca", "ra", "ka", "da", "ta", "sa", "wa", "la", "pa", "dha", "ja", "ya", "nya", "ma", "ga", "ba", "tha", "nga")
    private val pasangan = setOf("p_ha", "p_ca", "p_ra", "p_ka", "p_da", "p_ta", "p_sa", "p_la", "p_pa", "p_dha", "p_ja", "p_ya", "p_ma", "p_ga", "p_ba", "p_tha", "p_nga")
    private val swara = setOf("a", "i", "u", "e", "o")
    private val sVokal = setOf("wulu", "pepet", "tarung")
    private val sSigeg = setOf("layar", "wignyan", "cecak")
    private val semuaS = sVokal + sSigeg + setOf("taling", "pangkon")

    init {
        // 1. Muat OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.e("AksoroEngine", "OpenCV gagal dimuat!")
        }

        // 2. Muat Model PyTorch (MobileNetV2)
        try {
            val cnnPath = assetFilePath(context, "mobilenetv2_aksoro_final.ptl")
            val seq2seqPath = assetFilePath(context, "seq2seq_aksoro_final.ptl")
            cnnModule = LiteModuleLoader.load(cnnPath)
            seq2seqModule = LiteModuleLoader.load(seq2seqPath)
            Log.d("AksoroEngine", "MobileNetV2 berhasil dimuat!")
            Log.d("AksoroEngine", "Seq2Seq berhasil dimuat!")
        } catch (e: Exception) {
            Log.e("AksoroEngine", "Error memuat MobileNetV2: ${e.message}")
            Log.e("AksoroEngine", "Error memuat Seq2Seq: ${e.message}")
        }
    }

    // Fungsi utilitas untuk memindahkan file model dari 'assets' ke memori internal agar bisa dibaca PyTorch
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)

        // Selalu hapus file lama jika ada
        if (file.exists()) {
            file.delete()
        }

        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        return file.absolutePath
    }

    fun processImage(bitmap: Bitmap): PipelineResult {
        Log.d("Aksoro", "Memulai pemrosesan gambar OpenCV...")

        // 1. Jalankan Segmentasi OpenCV
        val (segmentedBitmaps, annotatedBmp) = segmentCharacters(bitmap)
        Log.d("Aksoro", "Berhasil memotong ${segmentedBitmaps.size} aksara")

        // 2. Looping potongan gambar ke PyTorch MobileNetV2
        val cnnResults = classifyCharacters(segmentedBitmaps)

        // 3. Looping hasil MobileNetV2 ke PyTorch Seq2Seq LSTM
        var finalLstmString = ""
        if (cnnResults.isNotEmpty()) {
            // --- LOG 1: ARRAY ASLI ---
            Log.d("Aksoro_Debug", "=== ARRAY KLASIFIKASI ===")
            Log.d("Aksoro_Debug", cnnResults.toString())

            val tokenBatches = chunkCnnTokens(cnnResults)
            val translatedBuilder = java.lang.StringBuilder()

            // --- LOG 2: HASIL CHUNKING ---
            Log.d("Aksoro_Debug", "=== HASIL CHUNKING ===")
            tokenBatches.forEachIndexed { idx, batch ->
                Log.d("Aksoro_Debug", "Batch ${idx + 1}: $batch")
            }

            // --- LOG 3: TRANSLITERASI PER BATCH ---
            for (i in tokenBatches.indices) {
                val batch = tokenBatches[i]
                val translatedChunk = translateSeq2Seq(batch)
                Log.d("Aksoro_Debug", "Hasil Translasi Batch ${i + 1}: $translatedChunk")
                translatedBuilder.append(translatedChunk)
            }

            finalLstmString = translatedBuilder.toString()
            // --- LOG 4: HASIL AKHIR ---
            Log.d("Aksoro_Debug", "=== HASIL GABUNGAN ===")
            Log.d("Aksoro_Debug", finalLstmString)
        } else {
            finalLstmString = "Aksara tidak terdeteksi."
        }

        return PipelineResult(
            cnnOutput = cnnResults,
            lstmOutput = finalLstmString,
            debugImage = annotatedBmp
        )
    }

    // ==============================================================================
    // FUNGSI INFERENSI PYTORCH (MobileNetV2)
    // ==============================================================================
    private fun classifyCharacters(charBitmaps: List<Bitmap>): List<String> {
        val results = mutableListOf<String>()
        if (cnnModule == null) return results

        for (bitmap in charBitmaps) {
            // 1. Resize gambar ke 224x224
            val resizedBmp = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            // 2. Pastikan background solid putih (Menghindari bug transparansi)
            val solidBmp = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(solidBmp)
            canvas.drawColor(android.graphics.Color.WHITE)
            canvas.drawBitmap(resizedBmp, 0f, 0f, null)

            // ==========================================================
            // JURUS PAMUNGKAS: BYPASS TENSORIMAGEUTILS (KONVERSI MANUAL)
            // ==========================================================
            val floatArray = FloatArray(3 * 224 * 224)
            val pixels = IntArray(224 * 224)

            // Sedot seluruh piksel dari gambar
            solidBmp.getPixels(pixels, 0, 224, 0, 0, 224, 224)

            var sumR = 0f

            // Lakukan perulangan persis seperti transforms.ToTensor()
            for (i in pixels.indices) {
                val color = pixels[i]
                // Ekstrak warna RGB dan ubah skala 0-255 menjadi 0.0 - 1.0
                val r = ((color shr 16) and 0xFF) / 255.0f
                val g = ((color shr 8) and 0xFF) / 255.0f
                val b = (color and 0xFF) / 255.0f

                sumR += r // Rekam tingkat kecerahan

                // Lakukan transforms.Normalize(mean, std) secara manual
                floatArray[i] = (r - 0.485f) / 0.229f                     // Saluran Merah (Red)
                floatArray[224 * 224 + i] = (g - 0.456f) / 0.224f         // Saluran Hijau (Green)
                floatArray[2 * 224 * 224 + i] = (b - 0.406f) / 0.225f     // Saluran Biru (Blue)
            }

            // sensor 1
            val avgColor = sumR / (224 * 224)
            // Log.d("Aksoro", "X-RAY GAMBAR -> Rata-rata kecerahan piksel (0.0 - 1.0): $avgColor")

            // Bentuk tensor 4 dimensi: [Batch=1, Channels=3, Height=224, Width=224]
            val tensor = org.pytorch.Tensor.fromBlob(floatArray, longArrayOf(1, 3, 224, 224))
            // ==========================================================

            // 3. Eksekusi model (Forward Pass)
            val outputTensor = cnnModule!!.forward(IValue.from(tensor)).toTensor()
            val logits = outputTensor.dataAsFloatArray

            // SENSOR 2: Pastikan model yang dimuat benar-benar memiliki 50 kelas
            // Log.d("Aksoro", "X-RAY MODEL -> Jumlah output neuron: ${logits.size}")

            // 4. Hitung Softmax manual
            val probs = calculateSoftmax(logits)

            var maxProb = -1f
            var maxIdx = -1
            for (i in probs.indices) {
                if (probs[i] > maxProb) {
                    maxProb = probs[i]
                    maxIdx = i
                }
            }

            // Intip apa yang sedang dipikirkan AI
            if (maxIdx != -1) {
                val tebakan = classNames[maxIdx]
                Log.d("Aksoro", "Prediksi Model: $tebakan | Confidence: $maxProb")
            }

            // 5. Filter dengan Threshold
            if (maxProb >= CONFIDENCE_THRESHOLD && maxIdx < classNames.size) {
                results.add(classNames[maxIdx])
            } else {
                Log.d("Aksoro", "Tebakan dibuang karena confidence di bawah ambang batas (0.25)")
            }
        }
        return results
    }

    private fun calculateSoftmax(logits: FloatArray): FloatArray {
        var maxLogit = -Float.MAX_VALUE
        for (logit in logits) {
            if (logit > maxLogit) maxLogit = logit
        }

        var sum = 0f
        val probs = FloatArray(logits.size)
        for (i in logits.indices) {
            probs[i] = exp((logits[i] - maxLogit).toDouble()).toFloat()
            sum += probs[i]
        }
        for (i in probs.indices) {
            probs[i] /= sum
        }
        return probs
    }

    // ==============================================================================
    // FUNGSI SEGMENTASI (Tidak ada yang diubah, sama seperti sebelumnya)
    // ==============================================================================
    private fun segmentCharacters(bitmap: Bitmap): Pair<List<Bitmap>, Bitmap> {
        val originalMat = Mat()
        Utils.bitmapToMat(bitmap, originalMat)

        val gray = Mat()
        Imgproc.cvtColor(originalMat, gray, Imgproc.COLOR_RGBA2GRAY)
        val blur = Mat()
        Imgproc.GaussianBlur(gray, blur, Size(5.0, 5.0), 0.0)

        val threshInv = Mat()
        Imgproc.threshold(blur, threshInv, 0.0, 255.0, Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU)

        val cleanedImgGray = Mat()
        Imgproc.threshold(blur, cleanedImgGray, 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)

        val kernel = Mat.ones(3, 10, CvType.CV_8U)
        val dilated = Mat()
        Imgproc.dilate(threshInv, dilated, kernel, Point(-1.0, -1.0), 1)

        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(dilated, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val rawBoxes = mutableListOf<Rect>()
        for (contour in contours) {
            val rect = Imgproc.boundingRect(contour)
            if (rect.width * rect.height >= 200 && rect.width > 5 && rect.height > 5) {
                rawBoxes.add(rect)
            }
        }

        val mergedBoxes = mergeCloseBoundingBoxes(rawBoxes)
        mergedBoxes.sortBy { it.x }

        // debug visual
        val annotatedMat = originalMat.clone()
        for (rect in mergedBoxes) {
            // Gambar kotak dengan warna Merah (R=255, G=0, B=0, Alpha=255) ketebalan 4 piksel
            Imgproc.rectangle(annotatedMat, rect.tl(), rect.br(), Scalar(255.0, 0.0, 0.0, 255.0), 4)
        }
        // Ubah gambar yang sudah dicoret merah menjadi Bitmap
        val annotatedBmp = Bitmap.createBitmap(annotatedMat.cols(), annotatedMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(annotatedMat, annotatedBmp)

        val charBitmaps = mutableListOf<Bitmap>()
        val padding = 10

        val cleanedImgBgr = Mat()
        Imgproc.cvtColor(cleanedImgGray, cleanedImgBgr, Imgproc.COLOR_GRAY2BGR)

        for (rect in mergedBoxes) {
            val x = max(0, rect.x)
            val y = max(0, rect.y)
            val w = min(rect.width, cleanedImgBgr.cols() - x)
            val h = min(rect.height, cleanedImgBgr.rows() - y)
            val safeRect = Rect(x, y, w, h)

            var charCrop = Mat(cleanedImgBgr, safeRect)
            charCrop = removeSmallArtifacts(charCrop)

            val side = max(w, h) + (padding * 2)
            val canvas = Mat(side, side, CvType.CV_8UC3, Scalar(255.0, 255.0, 255.0))

            val pasteX = (side - charCrop.cols()) / 2
            val pasteY = (side - charCrop.rows()) / 2
            val roi = Rect(pasteX, pasteY, charCrop.cols(), charCrop.rows())

            charCrop.copyTo(Mat(canvas, roi))

            val canvasRgba = Mat()
            Imgproc.cvtColor(canvas, canvasRgba, Imgproc.COLOR_BGR2RGBA)

            // Buat Bitmap dan masukkan matriks RGBA yang sudah dijamin akurat
            val charBmp = Bitmap.createBitmap(canvasRgba.cols(), canvasRgba.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(canvasRgba, charBmp)
            charBitmaps.add(charBmp)
        }
        return Pair(charBitmaps, annotatedBmp)
    }

    private fun mergeCloseBoundingBoxes(boxes: List<Rect>): MutableList<Rect> {
        if (boxes.isEmpty()) return mutableListOf()

        val sortedWidths = boxes.map { it.width }.sorted()
        val medianW = if (sortedWidths.size % 2 == 0) {
            (sortedWidths[sortedWidths.size / 2 - 1] + sortedWidths[sortedWidths.size / 2]) / 2.0
        } else {
            sortedWidths[sortedWidths.size / 2].toDouble()
        }

        val merged = mutableListOf<Rect>()
        val used = BooleanArray(boxes.size) { false }

        for (i in boxes.indices) {
            if (used[i]) continue
            var rect1 = boxes[i]
            var mergedInThisPass = true

            while (mergedInThisPass) {
                mergedInThisPass = false
                for (j in boxes.indices) {
                    if (i == j || used[j]) continue
                    val rect2 = boxes[j]

                    val ixMin = max(rect1.x, rect2.x)
                    val ixMax = min(rect1.x + rect1.width, rect2.x + rect2.width)
                    val iyMin = max(rect1.y, rect2.y)
                    val iyMax = min(rect1.y + rect1.height, rect2.y + rect2.height)

                    var overlapRatio = 0.0
                    if (ixMin < ixMax && iyMin < iyMax) {
                        val interArea = (ixMax - ixMin) * (iyMax - iyMin)
                        val minArea = min(rect1.width * rect1.height, rect2.width * rect2.height)
                        overlapRatio = if (minArea > 0) interArea.toDouble() / minArea else 0.0
                    }

                    val newX = min(rect1.x, rect2.x)
                    val newY = min(rect1.y, rect2.y)
                    val newW = max(rect1.x + rect1.width, rect2.x + rect2.width) - newX
                    val newH = max(rect1.y + rect1.height, rect2.y + rect2.height) - newY

                    if (Math.abs(rect1.y - rect2.y) < 40) {
                        if (overlapRatio > 0.60) {
                            // pass
                        }
                        // Hanya gabungkan jika tumpang tindih sedang (0.10 - 0.60)
                        else if (overlapRatio >= 0.10 && overlapRatio <= 0.60) {
                            if (newW < (medianW * 2.8)) {
                                rect1 = Rect(newX, newY, newW, newH)
                                used[j] = true
                                mergedInThisPass = true
                            }
                        }
                    }
                }
            }
            merged.add(rect1)
        }
        return merged
    }

    private fun removeSmallArtifacts(cropBgr: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(cropBgr, gray, Imgproc.COLOR_BGR2GRAY)

        val thresh = Mat()
        Imgproc.threshold(gray, thresh, 128.0, 255.0, Imgproc.THRESH_BINARY_INV)

        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        if (contours.size <= 1) return cropBgr

        var maxH = 0
        for (c in contours) {
            val h = Imgproc.boundingRect(c).height
            if (h > maxH) maxH = h
        }

        val mask = Mat.zeros(gray.size(), CvType.CV_8U)
        for (i in contours.indices) {
            val rect = Imgproc.boundingRect(contours[i])
            if (rect.height >= maxH * 0.75) {
                Imgproc.drawContours(mask, contours, i, Scalar(255.0), Core.FILLED)
            }
        }

        val cleanedResult = Mat(cropBgr.size(), cropBgr.type(), Scalar(255.0, 255.0, 255.0))
        cropBgr.copyTo(cleanedResult, mask)

        return cleanedResult
    }

    // ==============================================================================
    // FUNGSI INFERENSI SEQ2SEQ LSTM
    // ==============================================================================
    private fun chunkCnnTokens(cnnTokens: List<String>, minLen: Int = 3, maxLen: Int = 10): List<List<String>> {
        val batches = mutableListOf<List<String>>()
        val currentBatch = mutableListOf<String>()

        for (i in cnnTokens.indices) {
            val token = cnnTokens[i]
            currentBatch.add(token)
            val isLastToken = (i + 1 == cnnTokens.size)

            if (token == "pangkon") {
                batches.add(currentBatch.toList())
                currentBatch.clear()
                continue
            }

            if ((currentBatch.size >= minLen || currentBatch.size >= maxLen) && !isLastToken) {
                val nextToken = cnnTokens[i + 1]

                val isRed = (token in nglegena && nextToken in (pasangan + sVokal + "pangkon")) ||
                        (token in (sVokal + swara) && nextToken in sSigeg) ||
                        (token in pasangan && nextToken in (semuaS - "pangkon"))

                val isGreen = (token in (swara + "tarung")) ||
                        (nextToken in nglegena && token in (sSigeg + sVokal + pasangan + nglegena)) ||
                        (nextToken == "taling" && token in (sVokal + nglegena)) ||
                        (nextToken in swara && token in nglegena)

                if (isGreen && !isRed) {
                    batches.add(currentBatch.toList())
                    currentBatch.clear()
                }
            }
        }
        if (currentBatch.isNotEmpty()) {
            batches.add(currentBatch)
        }
        return batches
    }

    private fun encodeInput(seq: List<String>): List<Int> {
        val tokens = mutableListOf<Int>()
        tokens.add(inputVocab["<sos>"]!!)
        for (s in seq) {
            tokens.add(inputVocab[s] ?: inputVocab["<pad>"]!!)
        }
        tokens.add(inputVocab["<eos>"]!!)
        return tokens.take(MAX_INPUT_LEN)
    }

    private fun translateSeq2Seq(inputSeq: List<String>): String {
        if (seq2seqModule == null || inputSeq.isEmpty()) return ""

        // 1. Encoding & Padding
        val encoded = encodeInput(inputSeq).toMutableList()
        while (encoded.size < MAX_INPUT_LEN) {
            encoded.add(inputVocab["<pad>"]!!)
        }

        // 2. Buat LongTensor karena layer Embedding PyTorch hanya menerima tipe Int/Long
        val inTensorArray = LongArray(MAX_INPUT_LEN) { encoded[it].toLong() }
        val inputTensor = org.pytorch.Tensor.fromBlob(inTensorArray, longArrayOf(1, MAX_INPUT_LEN.toLong()))

        // 3. Eksekusi Model (Forward Pass)
        val outputTensor = seq2seqModule!!.forward(IValue.from(inputTensor)).toTensor()

        // Hasil dari TorchScript adalah Tensor Logits berdimensi [1, 20, 31 (vocab_size)]
        val logits = outputTensor.dataAsFloatArray
        val outputVocabSize = outputTokens.size

        // 4. Proses Decoding manual (Aman untuk Tensor 3D)
        val shape = outputTensor.shape()
        // shape biasanya [1, 20, 31] (Batch, Seq_Len, Vocab_Size)
        val seqLen = shape[1].toInt()
        val vocabSize = shape[2].toInt()

        val resultString = java.lang.StringBuilder()

        for (step in 0 until seqLen) {
            var maxVal = -Float.MAX_VALUE
            var maxIdx = -1

            // Rumus offset untuk tensor 3D [1, seqLen, vocabSize]
            val stepOffset = step * vocabSize

            for (i in 0 until vocabSize) {
                val logit = logits[stepOffset + i]
                if (logit > maxVal) {
                    maxVal = logit
                    maxIdx = i
                }
            }

            // Jika menabrak token <eos> (End of Sentence), hentikan pembacaan batch ini
            if (maxIdx == outputVocab["<eos>"]!!) {
                break
            }

            // Jika bukan token kontrol, gabungkan menjadi kata
            val char = invOutputVocab[maxIdx]
            if (char != "<sos>" && char != "<pad>") {
                resultString.append(char)
            }
        }

        return resultString.toString()
    }

    data class PipelineResult(
        val cnnOutput: List<String>,
        val lstmOutput: String,
        val debugImage: Bitmap? = null,
    )
}