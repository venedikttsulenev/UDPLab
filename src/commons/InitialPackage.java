package commons;

import java.io.Serializable;

public class InitialPackage implements Serializable {
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
    private final String fileName;
    private final int packageSize;

    public InitialPackage(long fileSize, String fileName, int packageSize) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.packageSize = packageSize;
    }

    @Override
    public String toString() {
        return "File '" + fileName + "', size: " + fileSize + ", split into packages of size " + packageSize;
    }
}
