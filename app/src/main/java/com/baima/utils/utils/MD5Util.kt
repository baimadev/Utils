package cn.com.fenrir.inc.anko.utils

import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and


object MD5 {
    private val hexDigits = charArrayOf(
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        'a',
        'b',
        'c',
        'd',
        'e',
        'f'
    )

    fun md5(input: String?): String? {
        return if (input == null) {
            null
        } else {
            try {
                val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")
                val inputByteArray = input.toByteArray()
                messageDigest.update(inputByteArray)
                val resultByteArray: ByteArray = messageDigest.digest()
                byteArrayToHex(resultByteArray)
            } catch (var4: NoSuchAlgorithmException) {
                null
            }
        }
    }

    fun md5(file: File): String? {
        try {
            if (!file.isFile()) {
                System.err.println(
                    "文件" + file.getAbsolutePath().toString() + "不存在或者不是文件"
                )
                return null
            }
            val `in` = FileInputStream(file)
            val result: String = md5(`in` as InputStream)
            `in`.close()
            return result
        } catch (var3: FileNotFoundException) {
            var3.printStackTrace()
        } catch (var4: IOException) {
            var4.printStackTrace()
        }
        return null
    }

    fun md5(`in`: InputStream): String {
        try {
            val messagedigest: MessageDigest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(1024)
            val var3 = false
            var read: Int=0
            while (`in`.read(buffer).also { read = it } != -1) {
                messagedigest.update(buffer, 0, read)
            }
            `in`.close()
            return byteArrayToHex(messagedigest.digest())
        } catch (var5: NoSuchAlgorithmException) {
            var5.printStackTrace()
        } catch (var6: FileNotFoundException) {
            var6.printStackTrace()
        } catch (var7: IOException) {
            var7.printStackTrace()
        }
        return ""
    }

    private fun byteArrayToHex(byteArray: ByteArray): String {
        val resultCharArray = CharArray(byteArray.size * 2)
        var index = 0
        val var5 = byteArray.size
        for (var4 in 0 until var5) {
            val b = byteArray[var4].toInt()

            resultCharArray[index++] = hexDigits[b ushr 4 and 15]
            resultCharArray[index++] = hexDigits[(b and 15)]
        }
        return String(resultCharArray)
    }
}

