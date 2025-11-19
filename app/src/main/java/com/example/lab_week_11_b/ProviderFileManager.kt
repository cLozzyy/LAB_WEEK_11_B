package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executor

class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {
    // Menghasilkan model data (FileInfo) untuk file.
    // Model data ini berisi URI, file, nama, path relatif,
    // dan tipe MIME dari file.
    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"
        // Mendapatkan objek File.
        // File akan disimpan di dalam folder yang didefinisikan
        // pada file_provider_paths.xml.
        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getPicturesFolder(),
            "image/jpeg"
        )
    }

    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"
        // Mendapatkan objek File.
        // File akan disimpan di dalam folder yang didefinisikan
        // pada file_provider_paths.xml.
        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )
        return FileInfo(
            fileHelper.getUriFromFile(file),
            file,
            name,
            fileHelper.getVideosFolder(),
            "video/mp4"
        )
    }

    // Memasukkan gambar/video ke MediaStore.
    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo,
                mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(it)
            )
        }
    }

    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                fileInfo,
                mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(it)
            )
        }
    }

    // Memasukkan file ke MediaStore.
    // File akan disalin ke path relatif yang diberikan.
    // InputStream digunakan untuk membaca file.
    // OutputStream digunakan untuk menulis file.
    private fun insertToStore(fileInfo: FileInfo, contentUri: Uri,
                              contentValues: ContentValues) {
        executor.execute {
            val insertedUri = contentResolver.insert(contentUri,
                contentValues)
            insertedUri?.let {
                val inputStream =
                    contentResolver.openInputStream(fileInfo.uri)
                val outputStream =
                    contentResolver.openOutputStream(insertedUri)
                IOUtils.copy(inputStream, outputStream)
            }
        }
    }
}
