package com.ashkin.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class NfcService : HostApduService() {
    companion object {
        // 用于识别特定的应用程序
        const val AID = "F0010203040506"

        // 模拟卡片数据
        private val CARD_DATA = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06)

        // 用于选择应用程序的APDU命令
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), // CLA (Class)
            0xA4.toByte(), // INS (Instruction)
            0x04.toByte(), // P1 (Parameter 1)
            0x00.toByte(), // P2 (Parameter 2)
            0x06.toByte(), // Lc (Length of data)
            0xF0.toByte(),
            0x01.toByte(),
            0x02.toByte(),
            0x03.toByte(),
            0x04.toByte(),
            0x05.toByte(),
            0x06.toByte() // AID
        )

        // 用于读取数据的 APDU 命令
        private val READ_DATA_APDU = byteArrayOf(
            0x00.toByte(), // CLA (Class)
            0xB0.toByte(), // INS (Instruction)
            0x00.toByte(), // P1 (Parameter 1)
            0x00.toByte(), // P2 (Parameter 2)
            0x00.toByte() // Le (Length of expected data)
        )
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d("TAG", "processCommandApdu: $commandApdu")
        return when {
            commandApdu.contentEquals(SELECT_APDU) -> selectAid()
            commandApdu.contentEquals(READ_DATA_APDU) -> readCardData()
            else -> byteArrayOf(0x6A.toByte(), 0x81.toByte()) // 返回错误状态码
        }
    }

    override fun onDeactivated(reason: Int) {
        // 模拟卡被停用时的回调
        Log.d("TAG", "onDeactivated: $reason")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "onCreate: NfcHostApduService")
    }

    private fun selectAid(): ByteArray {
        return byteArrayOf(0x90.toByte(), 0x00.toByte()) // 返回成功状态码
    }

    private fun readCardData(): ByteArray {
        return CARD_DATA
    }
}
