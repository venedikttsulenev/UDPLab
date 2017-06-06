package client;

import commons.InitialPackage;

import java.io.PrintStream;

public class Logger {
    private final PrintStream ps;
    private final boolean logPackages;
    private static Logger instance = new Logger(System.out, false);

    private Logger(PrintStream p, boolean logPackages) {
        this.ps = p;
        this.logPackages = logPackages;
    }

    public static Logger getInstance() {
        return instance;
    }

    void packageSent(int id) {
        if (logPackages) {
            if (ps != null) {
                if (id == InitialPackage.ID)
                    ps.println("Initial package sent");
                else
                    ps.println("Package #" + id + " sent");
            }
        }
    }

    void packageDelivered(int id) {
        if (logPackages) {
            if (ps != null) {
                if (id == InitialPackage.ID)
                    ps.println("Initial package delivered");
                else
                    ps.println("Package #" + id + " delivered");
            }
        }
    }

    void packageTimedOut(int id) {
        if (ps != null) {
            if (id == InitialPackage.ID)
                ps.println("Initial package timed out");
            else
                ps.println("Package #" + id + " timed out");
        }
    }

    void fileReaderFinished() {
        if (ps != null)
            ps.println("File reader finished");
    }

    void allPackagesSent() {
        if (ps != null)
            ps.println("All packages sent");
    }

    void allPackagesDelivered() {
        if (ps != null)
            ps.println("All packages delivered");
    }

    void fileSenderFinished() {
        if (ps != null)
            ps.println("File sender finished");
    }

    void acknowledgementReceiverFinished() {
        if (ps != null) {
            ps.println("Acknowledgement receiver finished");
        }
    }
}
