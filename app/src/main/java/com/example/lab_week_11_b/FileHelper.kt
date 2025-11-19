package com.example.lab_week_11_b

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class FileHelper(private val context: Context) {

    /**
     * Menghasilkan URI untuk mengakses file.
     * URI ini bersifat sementara untuk membatasi akses dari aplikasi lain.
     */
    fun getUriFromFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.example.lab_week_11_b.camera", // Pastikan authority ini cocok dengan yang ada di AndroidManifest.xml
            file
        )
    }

    /**
     * Mendapatkan nama folder untuk gambar.
     * Nama ini didefinisikan dalam file_provider_paths.xml.
     */
    fun getPicturesFolder(): String {
        return Environment.DIRECTORY_PICTURES
    }

    /**
     * Mendapatkan nama folder untuk video.
     * Nama ini didefinisikan dalam file_provider_paths.xml.
     */
    fun getVideosFolder(): String {
        return Environment.DIRECTORY_MOVIES
    }
}
