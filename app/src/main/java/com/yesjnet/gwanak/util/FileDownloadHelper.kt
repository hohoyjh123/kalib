package com.yesjnet.gwanak.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import java.net.URLConnection

object FileDownloadHelper {

    /**
     * 서버 응답에서 전달된 contentDisposition / mimeType / url 을 기반으로
     * 올바른 파일명과 MIME 타입을 추출하고 다운로드를 수행합니다.
     */
    fun downloadFile(
        context: Context,
        url: String,
        contentDisposition: String?,
        mimeType: String?
    ) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)

        // 1️⃣ 파일명 추출
        val fileName = guessFileNameFromHeader(url, contentDisposition, mimeType)

        // 2️⃣ MIME 타입 교정
        val correctedMimeType = correctMimeType(fileName, mimeType)

        // 3️⃣ 요청 구성
        val request = DownloadManager.Request(uri).apply {
            setTitle(fileName)
            setDescription("파일을 다운로드 중입니다…")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            allowScanningByMediaScanner()

            // 다운로드 폴더에 저장
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            // MIME 타입 명시
            setMimeType(correctedMimeType)

            // User-Agent 헤더 추가 (필수 아님)
            addRequestHeader("User-Agent", System.getProperty("http.agent"))
        }

        downloadManager.enqueue(request)
    }

    /**
     * Content-Disposition 과 URL 을 기반으로 파일명을 추정
     */
    private fun guessFileNameFromHeader(
        url: String,
        contentDisposition: String?,
        mimeType: String?
    ): String {
        var fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        if (fileName.isBlank()) fileName = "download_${System.currentTimeMillis()}"
        return fileName
    }

    /**
     * 파일 확장자 또는 MIME 정보를 기반으로 정확한 MIME 타입을 추정
     */
    fun correctMimeType(fileName: String, mimeType: String?): String {
        val lower = fileName.lowercase()

        return when {
            lower.endsWith(".txt") -> "text/plain; charset=utf-8"
            lower.endsWith(".csv") -> "text/csv; charset=utf-8"
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".xls") -> "application/vnd.ms-excel"
            lower.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            lower.endsWith(".doc") -> "application/msword"
            lower.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            lower.endsWith(".zip") -> "application/zip"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".mp4") -> "video/mp4"
            else -> {
                // 파일 확장자가 애매하면 URLConnection 기반 추론
                val guessed = URLConnection.guessContentTypeFromName(fileName)
                guessed ?: mimeType ?: "application/octet-stream"
            }
        }
    }
}