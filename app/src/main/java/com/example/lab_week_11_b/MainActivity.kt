package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
    }

    // Helper class untuk mengelola file di MediaStore
    private lateinit var providerFileManager: ProviderFileManager
    // Model data untuk file
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null
    // Flag untuk menandai apakah pengguna sedang mengambil foto atau video
    private var isCapturingVideo = false
    // Activity result launcher untuk mengambil gambar dan video
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var takeVideoLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi ProviderFileManager
        providerFileManager =
            ProviderFileManager(
                applicationContext,
                FileHelper(applicationContext),
                contentResolver,
                Executors.newSingleThreadExecutor(),
                MediaContentHelper()
            )

        // Inisialisasi activity result launcher
        // .TakePicture() dan .CaptureVideo() adalah contract bawaan
        // Digunakan untuk mengambil gambar dan video
        // Hasilnya akan disimpan di URI yang dikirim ke launcher
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                // Memasukkan gambar ke MediaStore jika pengambilan berhasil (success == true)
                if (success) {
                    providerFileManager.insertImageToStore(photoInfo)
                }
            }
        takeVideoLauncher =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
                // Memasukkan video ke MediaStore jika pengambilan berhasil (success == true)
                if (success) {
                    providerFileManager.insertVideoToStore(videoInfo)
                }
            }

        findViewById<Button>(R.id.photo_button).setOnClickListener {
            // Set flag bahwa pengguna akan mengambil foto
            isCapturingVideo = false
            // Periksa izin penyimpanan
            // Jika diizinkan, buka kamera. Jika tidak, minta izin.
            checkStoragePermission {
                openImageCapture()
            }
        }

        findViewById<Button>(R.id.video_button).setOnClickListener {
            // Set flag bahwa pengguna akan mengambil video
            isCapturingVideo = true
            // Periksa izin penyimpanan
            // Jika diizinkan, buka kamera. Jika tidak, minta izin.
            checkStoragePermission {
                openVideoCapture()
            }
        }
    }

    // Membuka kamera untuk mengambil gambar
    private fun openImageCapture() {
        photoInfo =
            providerFileManager.generatePhotoUri(System.currentTimeMillis())
        // Pastikan URI tidak null sebelum meluncurkan kamera
        photoInfo?.uri?.let {
            takePictureLauncher.launch(it)
        }
    }

    // Membuka kamera untuk merekam video
    private fun openVideoCapture() {
        videoInfo =
            providerFileManager.generateVideoUri(System.currentTimeMillis())
        // Pastikan URI tidak null sebelum meluncurkan kamera
        videoInfo?.uri?.let {
            takeVideoLauncher.launch(it)
        }
    }

    // Memeriksa izin penyimpanan
    // Untuk Android 10 (Q) ke atas, izin tulis tidak diperlukan untuk menyimpan ke koleksi media.
    // Untuk Android 9 (Pie) ke bawah, izin diperlukan.
    private fun checkStoragePermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Langsung jalankan aksi karena tidak perlu izin eksplisit untuk MediaStore
            onPermissionGranted()
        } else {
            // Periksa apakah izin WRITE_EXTERNAL_STORAGE sudah diberikan
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
                // Jika izin sudah diberikan
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                // Jika izin belum diberikan, minta izin kepada pengguna
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }

    // Untuk Android 9 ke bawah
    // Menangani hasil permintaan izin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            // Periksa apakah requestCode adalah untuk izin External Storage
            REQUEST_EXTERNAL_STORAGE -> {
                // Jika izin diberikan, buka kamera sesuai flag yang tersimpan
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
                // Jika izin ditolak, tidak ada aksi yang dilakukan.
                return
            }
        }
    }
}
