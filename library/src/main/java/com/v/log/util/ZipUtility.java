package com.v.log.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * author  : ww
 * desc    :
 * time    : 2023/11/29 17:50
 */
public class ZipUtility {

    /**
     * 将指定文件夹打包为ZIP文件
     *
     * @param sourceFolderPath 要打包的源文件夹路径
     * @param outputZipPath    输出ZIP文件路径
     */
    public static void zipFolder(String sourceFolderPath, String outputZipPath) {
        try {
            FileOutputStream outputStream = new FileOutputStream(outputZipPath);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            addFolderToZip(sourceFolderPath, sourceFolderPath, zipOutputStream);

            zipOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归地将文件夹中的文件添加到ZIP文件中
     *
     * @param folderPath       当前文件夹路径
     * @param baseFolderPath  基础文件夹路径（用于计算相对路径）
     * @param zipOutputStream ZIP输出流
     */
    private static void addFolderToZip(String folderPath, String baseFolderPath, ZipOutputStream zipOutputStream) {
        try {
            File folder = new File(folderPath);
            File[] fileList = folder.listFiles();

            for (File file : fileList) {
                if (file.isDirectory()) {
                    // 如果是子文件夹，则递归调用自身处理子文件夹
                    addFolderToZip(file.getPath(), baseFolderPath, zipOutputStream);
                } else {
                    // 如果是文件，则创建ZIP条目并将文件内容写入ZIP输出流
                    String relativePath = file.getPath().substring(baseFolderPath.length() + 1);
                    ZipEntry zipEntry = new ZipEntry(relativePath);
                    zipOutputStream.putNextEntry(zipEntry);

                    FileInputStream inputStream = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        zipOutputStream.write(buffer, 0, length);
                    }

                    inputStream.close();
                    zipOutputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}