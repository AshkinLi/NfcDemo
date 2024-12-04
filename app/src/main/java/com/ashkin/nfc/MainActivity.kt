package com.ashkin.nfc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ashkin.nfc.ui.screen.NfcScreen
import com.ashkin.nfc.ui.theme.NfcDemoTheme

class MainActivity : ComponentActivity() {

    private val nfcHelper by lazy { NfcHelper() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val nfcTagContent = remember { mutableStateOf("") }

            nfcHelper.handleNfcIntent(intent) {
                nfcTagContent.value = it
            }

            NfcDemoTheme {
                NfcScreen(
                    nfcHelper = nfcHelper,
                    nfcTagContent = nfcTagContent,
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                )
            }
        }
    }
}

