package com.ashkin.nfc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ashkin.nfc.ui.screen.NfcScreen
import com.ashkin.nfc.ui.theme.NfcDemoTheme

class MainActivity : ComponentActivity() {

    private val nfcHelper by lazy { NfcHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NfcDemoTheme {
                NfcScreen(
                    nfcHelper = nfcHelper,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        nfcHelper.handleNfcIntent(intent) {
            Log.d("NfcHelper", "onCreate: $it")
        }
    }
}

