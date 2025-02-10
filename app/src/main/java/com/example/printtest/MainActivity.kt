package com.example.bixolonprinter

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.bxl.config.editor.BXLConfigLoader
import jpos.POSPrinter
import jpos.POSPrinterConst
import jpos.JposException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.printtest.R

class MainActivity : ComponentActivity() {
    private lateinit var posPrinter: POSPrinter
    private lateinit var bxlConfigLoader: BXLConfigLoader
    private val logicalName = "BIXOLON"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        posPrinter = POSPrinter(this)
        bxlConfigLoader = BXLConfigLoader(this)

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

        configurePrinter()

        setContent {
            PrintScreen(onPrintClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    printImageAndText()
                }
            })
        }
    }

    private fun configurePrinter() {
        try {
            bxlConfigLoader.addEntry(
                logicalName,
                BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                "SPP-R200III",
                BXLConfigLoader.DEVICE_BUS_BLUETOOTH,
                "74:F0:7D:E5:91:F7"
            )
            bxlConfigLoader.saveFile()
            Log.d("BIXOLON", "✅ Settings saved successfully")
        } catch (e: Exception) {
            Log.e("BIXOLON", "❌ Error configuring printer", e)
        }
    }

    @Composable
    fun PrintScreen(onPrintClick: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onPrintClick) {
                Text("Print Ticket")
            }
        }
    }

    private fun printImageAndText() {
        try {
            posPrinter.open(logicalName)
            posPrinter.claim(5000)
            posPrinter.deviceEnabled = true

            // Transaction begins to group image and text
            posPrinter.transactionPrint(POSPrinterConst.PTR_S_RECEIPT, POSPrinterConst.PTR_TP_TRANSACTION)
            val bitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.logo)
            if (bitmap == null) {
                Log.e("Printer", "❌ Error: Image could not be loaded")
                return
            }

            val processedBitmap = convertToMonochrome(bitmap)


            posPrinter.printBitmap(
                POSPrinterConst.PTR_S_RECEIPT,
                processedBitmap,
                posPrinter.recLineWidth,
                POSPrinterConst.PTR_BM_CENTER,
                200
            )


            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\n\n")


            posPrinter.printNormal(
                POSPrinterConst.PTR_S_RECEIPT,
                "===== PURCHASE TICKET =====\nThank you for your purchase\n\n"
            )

            // Finalizar transacción e imprimir
            posPrinter.transactionPrint(POSPrinterConst.PTR_S_RECEIPT, POSPrinterConst.PTR_TP_NORMAL)

            // Cerrar la conexión
            posPrinter.close()

            Log.d("Printer", "✅ Printing completed successfully")
        } catch (e: JposException) {
            Log.e("Printer", "❌ Error printing ticket", e)
        }
    }

    /**
     * Converts an image to black and white (1-bit) for improved thermal printing.
     */
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
}
