package com.v.log.util

import android.content.Context
import com.v.log.VLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * author  : ww
 * desc    :
 * time    : 2023/12/28 19:26
 */
object ZipUtils {
    fun zip(context: Context): String {
        //提供了一个数据项压缩成一个ZIP归档输出流
        var out: ZipOutputStream? = null
        var outPath = ""
        try {
            VLog.flush()
            outPath = context.getExternalFilesDir(null)!!.absolutePath + "/logs.zip"
            val outFile = File(outPath) //源文件或者目录
            val fileOrDirectory = File(VLog.getDefaultLogPath()) //压缩文件路径
            out = ZipOutputStream(FileOutputStream(outFile))
            //如果此文件是一个文件，否则为false。
            if (fileOrDirectory.isFile) {
                zipFileOrDirectory(out, fileOrDirectory, "")
            } else {
                //返回一个文件或空阵列。
                val entries = fileOrDirectory.listFiles()
                for (i in entries.indices) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(out, entries[i], "")
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            outPath = ""
        } finally {
            //关闭输出流
            if (out != null) {
                try {
                    out.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
        return outPath
    }

    private fun zipFileOrDirectory(
        out: ZipOutputStream,
        fileOrDirectory: File, curPath: String
    ) {
        //从文件中读取字节的输入流
        var `in`: FileInputStream? = null
        try {
            //如果此文件是一个目录，否则返回false。
            if (!fileOrDirectory.isDirectory) {
                // 压缩文件
                val buffer = ByteArray(4096)
                var bytes_read: Int
                `in` = FileInputStream(fileOrDirectory)
                //实例代表一个条目内的ZIP归档
                val entry = ZipEntry(
                    curPath
                            + fileOrDirectory.name
                )
                //条目的信息写入底层流
                out.putNextEntry(entry)
                while (`in`.read(buffer).also { bytes_read = it } != -1) {
                    out.write(buffer, 0, bytes_read)
                }
                out.closeEntry()
            } else {
                // 压缩目录
                val entries = fileOrDirectory.listFiles()
                for (i in entries.indices) {
                    // 递归压缩，更新curPaths
                    zipFileOrDirectory(
                        out, entries[i], curPath
                                + fileOrDirectory.name + "/"
                    )
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            // throw ex;
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    fun unzip(zipFileName: String?, outputDirectory: String) {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(zipFileName)
            val e: Enumeration<*> = zipFile.entries()
            var zipEntry: ZipEntry? = null
            val dest = File(outputDirectory)
            dest.mkdirs()
            while (e.hasMoreElements()) {
                zipEntry = e.nextElement() as ZipEntry
                val entryName = zipEntry.name
                var `in`: InputStream? = null
                var out: FileOutputStream? = null
                try {
                    if (zipEntry!!.isDirectory) {
                        var name = zipEntry.name
                        name = name.substring(0, name.length - 1)
                        val f = File(
                            outputDirectory + File.separator
                                    + name
                        )
                        f.mkdirs()
                    } else {
                        var index = entryName.lastIndexOf("\\")
                        if (index != -1) {
                            val df = File(
                                outputDirectory + File.separator
                                        + entryName.substring(0, index)
                            )
                            df.mkdirs()
                        }
                        index = entryName.lastIndexOf("/")
                        if (index != -1) {
                            val df = File(
                                outputDirectory + File.separator
                                        + entryName.substring(0, index)
                            )
                            df.mkdirs()
                        }
                        val f = File(
                            outputDirectory + File.separator
                                    + zipEntry.name
                        )
                        // f.createNewFile();
                        `in` = zipFile.getInputStream(zipEntry)
                        out = FileOutputStream(f)
                        var c: Int
                        val by = ByteArray(1024)
                        while (`in`.read(by).also { c = it } != -1) {
                            out.write(by, 0, c)
                        }
                        out.flush()
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    throw IOException("解压失败：$ex")
                } finally {
                    if (`in` != null) {
                        try {
                            `in`.close()
                        } catch (ex: IOException) {
                        }
                    }
                    if (out != null) {
                        try {
                            out.close()
                        } catch (ex: IOException) {
                        }
                    }
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            throw IOException("解压失败：$ex")
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (ex: IOException) {
                }
            }
        }
    }
}