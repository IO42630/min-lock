package com.olexyn.min.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.util.Optional;

import com.olexyn.min.log.LogU;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public final class LockU {

    private static final int DEFAULT_LOCK_TRIES = 4;
    private static final long SLEEP_DURATION = 1000;

    private LockU() {
    }

    public static Optional<CFile> newFile(Path filePath) {
        try {
            var fc = FileChannel.open(filePath, CREATE_NEW, WRITE);
            return Optional.of(new CFile(filePath, fc, false));
        } catch (IOException | OverlappingFileLockException e) {
            LogU.warnPlain("Could not NEW %s", filePath, e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<CFile> lockFile(Path filePath) {
        return lockFile(filePath, DEFAULT_LOCK_TRIES);
    }

    public static Optional<CFile> lockFile(Path filePath, int tryCount) {
        try {
            var fc = FileChannel.open(filePath, READ, WRITE);
            if (filePath.toFile().exists()) {
                fc.lock();
            }
            return Optional.of(new CFile(filePath, fc, true));
        } catch (IOException | OverlappingFileLockException e) {
            if (tryCount > 0) {
                tryCount--;
                LogU.warnPlain("Could not lock %s. Will try %s times.", filePath, tryCount);
                try {
                    Thread.sleep(SLEEP_DURATION);
                } catch (InterruptedException ignored) {
                }
                return lockFile(filePath, tryCount);
            }
            LogU.warnPlain("Could not lock %s\n%s", filePath, e.getMessage());
            return Optional.empty();
        }
    }

    public static CFile unlockFile(CFile fcState, int tryCount) {
        return unlockFile(fcState.toPath(), fcState.getFc(), tryCount);
    }

    public static CFile unlockFile(Path filePath, FileChannel fc, int tryCount) {
        if (fc == null) {
            return null;
        }
        try {
            fc.close();
            return new CFile(filePath, fc, false);
        } catch (IOException | OverlappingFileLockException e) {
            if (tryCount > 0) {
                tryCount--;
                LogU.warnPlain("Could not close %s. Will try %s times.", fc, tryCount);

                return unlockFile(filePath, fc, tryCount);
            }
            LogU.warnPlain("Could not unlock %s", fc);
            return new CFile(filePath, null, true);
        }
    }
}
