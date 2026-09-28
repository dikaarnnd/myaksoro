package com.dika.myaksoro.ui.components

// --- Jetpack Compose: UI Units ---
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Token ukuran untuk komponen hasil transliterasi.
 * Home (besar) dan History/Riwayat Terbaru (kompak) memakai komponen yang sama, ukurannya beda.
 */
data class ResultStyle(
    val labelSize: TextUnit,
    val labelSpacing: TextUnit,
    val chipRadius: Dp,
    val chipPadH: Dp,
    val chipPadV: Dp,
    val chipFont: TextUnit,
    val chipGap: Dp,
    val listTopPad: Dp,
    val chunkRadius: Dp,
    val chunkPad: Dp,
    val chunkFont: TextUnit,
    val chunkLineHeight: TextUnit,
    val chunkGap: Dp,
    val resultRadius: Dp,
    val resultPadV: Dp,
    val resultPadH: Dp,
    val resultFont: TextUnit,
    val sectionGap: Dp,
    val resultLabelGap: Dp
) {
    companion object {
        val Home = ResultStyle(
            labelSize = 11.sp,
            labelSpacing = 1.5.sp,
            chipRadius = 8.dp,
            chipPadH = 12.dp,
            chipPadV = 6.dp,
            chipFont = 13.sp,
            chipGap = 8.dp,
            listTopPad = 10.dp,
            chunkRadius = 8.dp,
            chunkPad = 14.dp,
            chunkFont = 14.sp,
            chunkLineHeight = 22.sp,
            chunkGap = 8.dp,
            resultRadius = 12.dp,
            resultPadV = 20.dp,
            resultPadH = 16.dp,
            resultFont = 24.sp,
            sectionGap = 20.dp,
            resultLabelGap = 10.dp
        )

        val Compact = ResultStyle(
            labelSize = 10.sp,
            labelSpacing = 1.2.sp,
            chipRadius = 6.dp,
            chipPadH = 8.dp,
            chipPadV = 4.dp,
            chipFont = 12.sp,
            chipGap = 6.dp,
            listTopPad = 8.dp,
            chunkRadius = 6.dp,
            chunkPad = 10.dp,
            chunkFont = 13.sp,
            chunkLineHeight = TextUnit.Unspecified,
            chunkGap = 6.dp,
            resultRadius = 8.dp,
            resultPadV = 16.dp,
            resultPadH = 16.dp,
            resultFont = 20.sp,
            sectionGap = 16.dp,
            resultLabelGap = 8.dp
        )
    }
}