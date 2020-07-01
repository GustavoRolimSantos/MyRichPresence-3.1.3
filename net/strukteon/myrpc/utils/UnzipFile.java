package net.strukteon.myrpc.utils;

import java.util.zip.*;
import java.io.*;

public class UnzipFile
{
    public static void unzip(final File zipFile, final File destDir) throws IOException {
        final byte[] buffer = new byte[1024];
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            final File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            }
            else {
                newFile.createNewFile();
                final FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
        }
        zis.closeEntry();
        zis.close();
    }
    
    public static File newFile(final File destinationDir, final ZipEntry zipEntry) throws IOException {
        final File destFile = new File(destinationDir, zipEntry.getName());
        final String destDirPath = destinationDir.getCanonicalPath();
        final String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
