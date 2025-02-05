package com.example.bixolonprinter

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bixolon.printer.BixolonPrinter
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : ComponentActivity() {

    private var bixolonPrinter: BixolonPrinter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val printerMacAddress = "74:F0:7D:E5:91:F7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val handler = Handler(Looper.getMainLooper())
        bixolonPrinter = BixolonPrinter(this, handler, Looper.getMainLooper())

        setContent {
            PrinterApp(
                onPrint = { printText("Test in Bixolon SPP-R200III") }
            )
        }
    }

    /** Bluetooth permissions on Android 12+ */
    private fun checkBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
        }
    }

    /** Bluetooth connection with the printer. */
    private fun connectToPrinter(): Boolean {
        if (!checkBluetoothPermission()) {
            requestBluetoothPermission()
            return false
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.find { it.address == printerMacAddress }

        return try {
            val uuid = SERIAL_PORT_SERVICE_CLASS_UUID
            bluetoothSocket = device?.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: IOException) {
            Log.e("Printer", "❌ Error conectando a la impresora", e)
            false
        }
    }

    /** Function to send text to the printer. */
    private fun printText(text: String) {
        if (connectToPrinter()) {
            try {
                val esc = byteArrayOf(0x1B, 0x40) // Initialize the printer
                outputStream?.write(esc)
                outputStream?.write("$text\n".toByteArray())
                outputStream?.write(byteArrayOf(0x0A))
                outputStream?.flush()
            } catch (e: IOException) {
                Log.e("Printer", "❌ Printing error", e)
            } finally {
                closeConnection()
            }
        }
    }

    /** Close Bluetooth connection */
    private fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("Printer", "❌ Error,  Close connection", e)
        }
    }

    companion object {
        private val SERIAL_PORT_SERVICE_CLASS_UUID: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}

/** UI */
@Composable
fun PrinterApp(onPrint: () -> Unit) {
    var isPrinting by remember { mutableStateOf(false) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Log.e("Printer", "❌ Bluetooth permission denied")
            }
        }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        LaunchedEffect(Unit) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🖨️ Printing with Bixolon SPP-R200III")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isPrinting = true
                onPrint()
                isPrinting = false
            },
            enabled = !isPrinting
        ) {
            Text(text = if (isPrinting) "Printing..." else "Print 'Hello World'")
        }
    }
}
