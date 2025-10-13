package com.yesjnet.gwanak.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.yesjnet.gwanak.core.ConstsData
import java.io.File

/**
 * 'Device Storage - File Management Class'
**/

class FileManager {

    companion object {

        // Filer Provider Sample
        fun fileProvider (): Boolean {

            return true
        }

        fun makeDirectory(path: String?): Boolean {
            return try {
                val folder = File(path)
                folder.mkdirs()
            } catch (e: Throwable) {
                false
            }
        }

        /**
         * 폴더 삭제(하위 파일도 삭제)
         * @param context context
         * @param dirName 폴더 경로
         */
        fun deleteFolder(context: Context, dirName: String = ""): Boolean {
            var folder = dirName
            if (folder.isNullOrBlank()) folder = getTempPhotoFilePath(context)

            var file = File(folder)
            return try {
                var childFileList = file.listFiles()

                for (childFile in childFileList) {
                    if (childFile.isDirectory) {
                        deleteFolder(context, childFile.absolutePath)
                    } else {
                        childFile.delete()
                    }
                }
                file.delete()
                true
            } catch (e: Exception) {
                false
            }
        }

        /**
         *  임시파일 경로
         */
        fun getTempPhotoFilePath(context: Context) : String{
            return "${context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/${ConstsData.TEMP_PATH}"
        }

        fun getTempVoiceFilePath(context: Context): String {
            return "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/${ConstsData.TEMP_PATH}"
        }

        /**
         * 단일파일 or 리스트파일 인덱스 구분
         */
        fun getTempPhotoFileName(idx:Int = -1) : String{
            // idx 가 없는 경우 중복되지 않는 파일로 생성
            val strIndex = if(idx >= 0) "_$idx" else "_${System.currentTimeMillis()}"
            return "${ConstsData.PHOTO_TEMP_FILE_NAME}${strIndex}${ConstsData.PHOTO_TEMP_FILE_EXT}"
        }

        fun getIntentForAlbumOutput(applicationContext: Context) : Intent {
            val intent: Intent
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//                intent = Intent(
//                    Intent.ACTION_PICK,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                )
//            } else {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "image/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
//            }
            return intent
        }

        fun getIntentUriForCameraOutput(applicationContext: Context, idx: Int = 0) : Pair<Intent,Uri> {
            var uri :Uri
            val tempFilePath = getTempPhotoFilePath(applicationContext)
            makeDirectory(tempFilePath)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val fileName: String = getTempPhotoFileName(idx)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                uri = FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}.provider",
                    File(tempFilePath, fileName)
                )
            } else {
                uri = Uri.fromFile(File(tempFilePath,fileName))
                intent.putExtra("return-data", true)
            }
            return Pair(intent,uri)
        }

    }
}

