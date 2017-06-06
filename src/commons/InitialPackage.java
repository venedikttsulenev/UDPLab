package commons;

import java.io.*;

public class InitialPackage extends Package {
    public static final int ID = -1;

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPackageSize() {
        return packageSize;
    }

    private final long fileSize;
    private final int packageSize;
    private final String fileName;

    public InitialPackage(String fileName, long fileSize, int packageSize) {
        super(ID);
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.packageSize = packageSize;
    }

    @Override
    public String toString() {
        return "File '" + fileName
                + "', size: " + fileSize
                + ", split into " + ((fileSize - 1) / packageSize + 1)
                + " packages of size " + packageSize;
    }
}
