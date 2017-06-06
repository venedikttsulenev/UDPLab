package commons;

import java.io.Serializable;

abstract public class Package implements Serializable {
    private transient volatile boolean delivered = false;
    private int packageNumber;

    Package(int packageNumber) {
        this.packageNumber = packageNumber;
    }

    public boolean isDelivered() {
        return delivered;
    }

    final public int getPackageNumber() {
        return packageNumber;
    }

    public void onDeliver() {
        delivered = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (!(obj instanceof Package))
            return false;
        Package pack = (Package) obj;
        return pack.packageNumber == packageNumber;
    }

    @Override
    public int hashCode() {
        return packageNumber;
    }
}
