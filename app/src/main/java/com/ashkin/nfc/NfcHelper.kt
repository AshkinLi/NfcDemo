package com.ashkin.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.util.Log
import androidx.core.util.Consumer

class NfcHelper {
    /**
     * Checks the state of the NFC adapter.
     *
     * @param context The context to use for getting the NFC adapter.
     * @return An integer representing the NFC state:
     *         - `NFC_STATE_UNAVAILABLE` if the device does not support NFC.
     *         - `NFC_STATE_DISABLE` if NFC is not enabled.
     *         - `NFC_STATE_ENABLE` if NFC is enabled.
     */
    fun checkNfcState(context: Context): Int {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return if (nfcAdapter == null) {
            // Device does not support NFC
            NFC_STATE_UNAVAILABLE
        } else if (!nfcAdapter.isEnabled) {
            // NFC is not enabled
            NFC_STATE_DISABLE
        } else {
            NFC_STATE_ENABLE
        }
    }

    /**
     * Enables NFC intent for foreground dispatch.
     *
     * This method should be called in `onResume` or later to enable NFC intent handling.
     * It makes the application the priority handler for NFC events when it is in the foreground.
     *
     * @param activity The current activity, providing context.
     * @param clz The class of the activity to handle the NFC intent.
     * @throws IllegalStateException if NFC is not enabled.
     */
    fun enableNfcIntent(activity: Activity, clz: Class<out Activity>) {
        if (checkNfcState(activity) != NFC_STATE_ENABLE) {
            throw IllegalStateException("NFC un!")
        }

        val intent = Intent(activity, clz).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        // remove the old pending intent if it exists
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)

        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        // Add three intent filters to listen for three types of NFC events
        val intentFiltersArray = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )

        // filter for NfcA
        val techListsArray = arrayOf(
            arrayOf(
                "android.nfc.tech.NfcA"
            )
        )

        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        nfcAdapter?.enableForegroundDispatch(
            activity,
            pendingIntent,
            intentFiltersArray,
            techListsArray
        )
    }

    /**
     * Disables NFC intent for foreground dispatch.
     *
     * This method should be called in `onPause` or earlier to disable NFC intent handling.
     * It removes the application as the priority handler for NFC events when it is in the foreground.
     *
     * @param activity The current activity, providing context.
     * @throws IllegalStateException if NFC is not enabled.
     */
    fun disableNfcIntent(activity: Activity) {
        if (checkNfcState(activity) != Companion.NFC_STATE_ENABLE) {
            throw IllegalStateException("NFC UNVALUABLE or UNOPEN!")
        }

        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    /**
     * Handles the NFC intent and processes the NFC tag data.
     *
     * @param intent The intent containing the NFC tag data.
     * @param callback The callback to handle the processed NFC data.
     */
    fun handleNfcIntent(intent: Intent, callback: Consumer<String>) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        Log.d("TAG", "handleNfcIntent ${intent.action}: $tag, $intent")

        when (intent.action) {
            // Triggered when NDEF (NFC Data Exchange Format) data is discovered, highest priority
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val ndefMessage = getNdefMessageFromTag(tag)

                if (ndefMessage != null) {
                    val records = ndefMessage.records
                    if (records != null && records.isNotEmpty()) {
                        val ndefRecord = records[0]
                        val payload = String(ndefRecord.payload)
                        // Handle the read NFC data here
                        callback.accept(payload)
                    }
                }
            }
            // Triggered when a specific technology (Tech) tag is discovered, second priority
            NfcAdapter.ACTION_TECH_DISCOVERED -> {

                // Determine the technology type of the tag
                tag?.techList?.let {

                    // Parse an NfcA tag
                    if (it.contains("android.nfc.tech.NfcA")) {
                        callback.accept(readNfcATag(tag))
                    }
                }
            }
            // Triggered when any type of tag is discovered, fallback intent
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                Log.d("TAG", "handleNfcIntent tag: $tag")
            }
        }
    }

    /**
     * Retrieves the NDEF message from the given NFC tag.
     *
     * @param tag The NFC tag from which to retrieve the NDEF message.
     * @return The NDEF message if available, or null if not.
     */
    private fun getNdefMessageFromTag(tag: Tag?): NdefMessage? {
        val ndef = Ndef.get(tag)
        ndef?.let {
            try {
                it.connect()
                return it.ndefMessage
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    it.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    /**
     * Reads data from an NfcA tag.
     *
     * @param tag The NFC tag to read from.
     * @return A string containing the read data.
     */
    private fun readNfcATag(tag: Tag): String {
        val result = StringBuilder()
        val nfcA = NfcA.get(tag)

        try {
            // 注意读取的时候不要拿开手机！！！
            nfcA.connect()

            // 读取信息
            result.appendLine("nfcA.sak: " + nfcA.sak)
            result.appendLine("nfcA.atqa: " + nfcA.atqa)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            nfcA.close()
        }

        return result.toString()
    }


    companion object {
        // NFC unavailable
        const val NFC_STATE_UNAVAILABLE = 0
        const val NFC_STATE_DISABLE = 1
        const val NFC_STATE_ENABLE = 2
    }
}
