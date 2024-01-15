package uz.mahmudxon.qrscanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
            binding.flashlight.isVisible = !binding.scannerView.isTorchEnable()
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

    override fun onStart() {
        super.onStart()
        binding.scannerView.startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.scannerView.stopCamera()
    }
}