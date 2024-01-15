package uz.mahmudxon.qrscanner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import uz.mahmudxon.qrscanner.databinding.QrScannerLayoutBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: QrScannerLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QrScannerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cameraSwitchButton.setOnClickListener {
            binding.scannerView.switchCamera()
            binding.flashlight.setImageResource(R.drawable.ic_flash_off_24)
        }

        binding.flashlight.setOnClickListener {
            if (binding.scannerView.isFlashlightOn) binding.scannerView.turnOffTorch()
            else binding.scannerView.turnOnTorch()

            binding.flashlight.setImageResource(
                if (binding.scannerView.isFlashlightOn) R.drawable.ic_flash_on_24
                else R.drawable.ic_flash_off_24
            )
        }

        binding.scannerView.listener = {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.scannerView.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        )
            activityResultLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
        else {
            startCamera()
        }
    }

    private fun startCamera() {
        hideSystemUI()
        binding.scannerView.startCamera()
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in permissions && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                openPermissionSettings()
            } else {
                startCamera()
            }
        }

    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(
            window,
            binding.root
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}