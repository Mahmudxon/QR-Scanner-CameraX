# ScannerView

ScannerView is a custom Android view designed to simplify QR code scanning within your Android application. It encapsulates the camera functionality, providing an easy-to-use interface for capturing and analyzing QR codes.

## Features

- QR code scanning using the device's camera
- Toggle flashlight on/off during scanning
- Switch between front and back cameras
- Customizable QR code filtering

## Installation

1. Add the following dependency to your app's `build.gradle` file:

```gradle

repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}

dependencies {
	        implementation 'com.github.Mahmudxon:QR-Scanner-CameraX:1.1.0'
	}
```

2. Sync your project to apply the changes.

## Usage

### Add ScannerView to your layout

```xml
<uz.mahmudxon.scanner.ScannerView
    android:id="@+id/scannerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

### Initialize ScannerView in your activity or fragment

```kotlin
val scannerView = findViewById<ScannerView>(R.id.scannerView)

// Set optional filter for QR codes
val scannerFilter = ScannerFilter() // Customize filter as needed
scannerView.setFilter(scannerFilter)

// Set listener for QR code results
scannerView.listener = { qrCodes ->
    // Handle QR code results
    Log.d("ScannerView", "QR Codes: $qrCodes")
}

// Start the camera
scannerView.startCamera()
```

### Additional functions

```kotlin
// Toggle flashlight
scannerView.turnOnTorch()
scannerView.turnOffTorch()

// Switch camera
scannerView.switchCamera()

// Start/Stop QR code analysis
scannerView.startAnalyze()
scannerView.stopAnalyze()

// Stop the camera (call in onDestroy or when no longer needed)
scannerView.stopCamera()
```

## License

This project is licensed under the [Apache License 2.0](LICENSE.md) - see the [LICENSE.md](LICENSE.md) file for details.
