# Bixolon Printer Integration Example for Android (Jetpack Compose)

This project demonstrates how to integrate and utilize a **Bixolon printer** within an **Android application using Jetpack Compose**. This implementation is based on the **official Bixolon SDK**, ensuring compatibility and optimal performance with supported Bixolon printers.

## Overview

The application establishes a **Bluetooth connection** to a **Bixolon SPP-R200III** printer and sends both **text and image data** for printing. The integration utilizes **`jPOS` and `BXLConfigLoader`** from the official **Bixolon SDK**, following best practices for Bluetooth printing.

## Features

- **Bluetooth Connectivity**: Configures and connects to the Bixolon printer using its **MAC address**.
- **Image and Text Printing**: Sends a logo and formatted text to the printer.
- **Permissions Handling**: Ensures proper permission requests for **Android 12+ (API level 31 and above)**.
- **Jetpack Compose UI**: Provides a simple UI with a button to trigger the printing process.

## Prerequisites

- **Android Device**: Running **Android API level 31 (Android 12)** or higher.
- **Bixolon Printer**: Specifically tested with **Bixolon SPP-R200III**.
- **Bixolon SDK**: The required libraries should be included in your project.

## Project Structure

- **`MainActivity.kt`**: Handles Bluetooth configuration, connection, and printing logic.
- **`PrintScreen.kt`**: Jetpack Compose UI for triggering printing.
- **`AndroidManifest.xml`**: Declares necessary permissions and Bluetooth features.

## Setup and Usage

1. **Clone the Repository**: Begin by cloning this repository to your local machine.

2. **Include the SDK**: Ensure that the **Bixolon SDK** (including `jPOS` and `BXLConfigLoader`) is included in your project dependencies.

3. **Configure Permissions**: Add the following permissions in your `AndroidManifest.xml`:

   ```xml
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
   <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
   ```

4. **Set Up Bluetooth**: Pair your Android device with the **Bixolon printer** via **Bluetooth settings**.

5. **Update MAC Address**: In `MainActivity.kt`, update the **`printerMacAddress`** variable with your printer's MAC address:

   ```kotlin
   private val printerMacAddress = "YOUR_PRINTER_MAC_ADDRESS"
   ```

6. **Build and Run**: Compile and run the application on your Android device. Use the UI to initiate a **test print**.

## Implementation Details

### 1. Configuring the Printer

The **BXLConfigLoader** is used to configure the printer for Bluetooth communication:

```kotlin
bxlConfigLoader.addEntry(
    logicalName,
    BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
    "SPP-R200III",
    BXLConfigLoader.DEVICE_BUS_BLUETOOTH,
    "74:F0:7D:E5:91:F7" // Replace with your printer MAC address
)
bxlConfigLoader.saveFile()
```

### 2. Printing an Image and Text

The application prints a **logo image** followed by **formatted text**:

```kotlin
val bitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.logo)
if (bitmap != null) {
    val processedBitmap = convertToMonochrome(bitmap)
    posPrinter.printBitmap(
        POSPrinterConst.PTR_S_RECEIPT,
        processedBitmap,
        posPrinter.recLineWidth,
        POSPrinterConst.PTR_BM_CENTER,
        200
    )
    posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n")
}
posPrinter.printNormal(
    POSPrinterConst.PTR_S_RECEIPT,
    "===== PURCHASE TICKET =====\nThank you for your purchase\n\n"
)
```

### 3. Handling Permissions in Android 12+

For **Android 12+**, Bluetooth permissions must be requested at runtime:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        ),
        1
    )
}
```

### 4. Converting Image for Thermal Printing

To improve **image printing quality**, the bitmap is converted to a **monochrome (black-and-white) format**:

```kotlin
private fun convertToMonochrome(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    val monochromeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = bitmap.getPixel(x, y)
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF
            val gray = (red + green + blue) / 3
            val newColor = if (gray > 128) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            monochromeBitmap.setPixel(x, y, newColor)
        }
    }
    return monochromeBitmap
}
```

## References

- **Official Bixolon SDK Documentation**: [https://www.bixolon.com/](https://www.bixolon.com/)

