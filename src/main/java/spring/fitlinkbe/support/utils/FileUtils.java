package spring.fitlinkbe.support.utils;

public class FileUtils {

    private static String extractFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
