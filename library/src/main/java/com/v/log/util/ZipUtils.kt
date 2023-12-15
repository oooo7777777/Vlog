package com.v.log.util


import android.content.Context
import com.v.log.VLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * author  : ww
 * desc    :
 * time    : 2023/11/29 17:54
 */
object ZipUtils {

    private fun getZipOutPutPath(context: Context): String {
        val mPath = File(context.getExternalFilesDir(null), "").absolutePath + "/logs.zip"
        val logFile = File(mPath)
        if (!logFile.exists()) {
            logFile.mkdirs()
        }
        return mPath
    }

    // 打包文件夹为 ZIP，并返回 ZIP 文件的地址
    suspend fun zipFolder(context: Context): String = coroutineScope {
        // 定义 ZIP 文件的保存路径

        val zipFilePath = getZipOutPutPath(context)

        // 创建用于写入 ZIP 文件的 ZipOutputStream 对象
        val zipOutputStream = ZipOutputStream(FileOutputStream(zipFilePath))

        // 获取源文件夹路径对应的 File 对象
        val sourceFolder = File(VLog.getDefaultLogPath())

        // 获取源文件夹下的所有文件和文件夹
        val files = sourceFolder.listFiles()

        // 利用协程并行处理文件和文件夹的打包
        files?.map { file ->
            async(Dispatchers.IO) {
                if (file.isDirectory) {
                    // 如果是文件夹，则递归打包文件夹
                    zipDirectory(file, "", zipOutputStream)
                } else {
                    // 如果是文件，则直接打包文件
                    zipFile(file, zipOutputStream)
                }
            }
        }?.awaitAll()

        // 关闭 ZipOutputStream
        zipOutputStream.close()

        // 返回 ZIP 文件的地址
        zipFilePath
    }

    // 递归打包文件夹
    private suspend fun zipDirectory(
        directory: File,
        parentPath: String,
        zipOutputStream: ZipOutputStream
    ) {
        // 获取文件夹下的所有文件和文件夹
        val files = directory.listFiles()

        // 利用协程并行处理文件和文件夹的打包
        files?.map { file ->
            CoroutineScope(Dispatchers.Main).async(Dispatchers.IO) {
                // 构建文件或文件夹的相对路径
                val relativePath =
                    if (parentPath.isNotEmpty()) "$parentPath/${file.name}" else file.name

                if (file.isDirectory) {
                    // 如果是文件夹，则递归打包文件夹
                    zipDirectory(file, relativePath, zipOutputStream)
                } else {
                    // 如果是文件，则直接打包文件
                    zipFile(file, relativePath, zipOutputStream)
                }
            }
        }?.awaitAll()

    }

    // 打包单个文件
    private fun zipFile(file: File, zipOutputStream: ZipOutputStream) {
        val buffer = ByteArray(1024)
        val fileInputStream = FileInputStream(file).buffered()

        // 创建 ZipEntry 对象，并将其添加到 ZipOutputStream 中
        val zipEntry = ZipEntry(file.name)
        zipOutputStream.putNextEntry(zipEntry)

        var bytesRead: Int
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
            // 将文件内容写入 ZipOutputStream
            zipOutputStream.write(buffer, 0, bytesRead)
        }

        // 关闭当前 ZipEntry
        zipOutputStream.closeEntry()

        // 关闭文件输入流
        fileInputStream.close()
    }

    // 打包单个文件，指定相对路径
    private fun zipFile(
        file: File,
        relativePath: String,
        zipOutputStream: ZipOutputStream
    ) {
        val buffer = ByteArray(1024)
        val fileInputStream = FileInputStream(file).buffered()

        // 创建 ZipEntry 对象，并将其添加到 ZipOutputStream 中
        val zipEntry = ZipEntry(relativePath)
        zipOutputStream.putNextEntry(zipEntry)

        var bytesRead: Int
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
            // 将文件内容写入 ZipOutputStream
            zipOutputStream.write(buffer, 0, bytesRead)
        }

        // 关闭当前 ZipEntry
        zipOutputStream.closeEntry()

        // 关闭文件输入流
        fileInputStream.close()
    }
}
