package cn.com.fenrir.inc.anko.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 可以看到，当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，
 * 否则就调用getCacheDir()方法来获取缓存路径。前者获取到的就是 /sdcard/Android/data/<application package>/cache 这个路径，
 * 而后者获取到的是 /data/data/<application package>/cache 这个路径。
 *
 * 获取缓存目录（并未创建）file.mkdirs()创建
 */
fun getDiskCacheDir(context: Context, uniqueName: String): File {
    val path =
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            context.externalCacheDir?.path
        } else {
            context.cacheDir.path
        }
    return File(path + File.separator + uniqueName)
}
