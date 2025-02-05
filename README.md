# Bixolon Printer Integration Example for Android

This project demonstrates how to integrate and utilize a Bixolon printer within an Android application. Given the limited official documentation, this example serves as a practical guide for developers aiming to implement Bixolon printer functionality in their Android projects.

## Overview

The application establishes a Bluetooth connection to a Bixolon SPP-R200III printer and sends text data for printing. The integration leverages the `BixolonPrinter.jar` SDK, sourced from the [fewlaps/bixolon-printer-example](https://github.com/fewlaps/bixolon-printer-example) repository, due to challenges encountered with the official SDK versions.

## Features

- **Bluetooth Connectivity**: Automatically connects to the Bixolon printer using its MAC address.
- **Text Printing**: Sends text data to the printer for immediate printing.
- **User Interface**: Provides a simple UI with a button to initiate the printing process.

## Prerequisites

- **Android Device**: Running Android API level 31 or higher.
- **Bixolon Printer**: Specifically tested with the Bixolon SPP-R200III model.
- **BixolonPrinter.jar**: Ensure this SDK is included in your project's `libs` directory.

## Project Structure

- **MainActivity.kt**: Contains the core logic for Bluetooth connection and printing.
- **PrinterApp Composable**: Defines the UI components using Jetpack Compose.
- **AndroidManifest.xml**: Declares necessary permissions and SDK versions.

## Setup and Usage

1. **Clone the Repository**: Begin by cloning this repository to your local machine.

2. **Include the SDK**: Place the `BixolonPrinter.jar` file into the `libs` directory of your project.

3. **Configure Permissions**: Ensure the following permissions are declared in your `AndroidManifest.xml`:

   ```xml
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
   <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
   ```

4. **Set Up Bluetooth**: Pair your Android device with the Bixolon printer via Bluetooth settings.

5. **Update MAC Address**: In `MainActivity.kt`, update the `printerMacAddress` variable with your printer's MAC address:

   ```kotlin
   private val printerMacAddress = "YOUR_PRINTER_MAC_ADDRESS"
   ```

6. **Build and Run**: Compile and run the application on your Android device. Use the provided UI to initiate a test print.

## Code Highlights

- **Bluetooth Connection**: The application checks for necessary Bluetooth permissions and establishes a connection to the printer's MAC address.

- **Printing Logic**: Utilizes the `OutputStream` to send initialization commands and text data to the printer.

- **Permissions Handling**: Implements runtime permission requests for Bluetooth connectivity, especially for devices running Android 12 (API level 31) and above.

## References

- [fewlaps/bixolon-printer-example](https://github.com/fewlaps/bixolon-printer-example): Provided the `BixolonPrinter.jar` SDK and served as a foundational reference for this implementation.



