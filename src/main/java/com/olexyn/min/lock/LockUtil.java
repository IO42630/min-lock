package com.olexyn.min.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;

import com.olexyn.min.log.LogU;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LockUtil {


    public static FcState lock(FcState fcState) {
        try {
            if (fcState.isFileExists()) {
                fcState.getFc().lock();
            }
            fcState.setLocked(true);
        } catch (IOException | OverlappingFileLockException e) {
            LogU.warnPlain("Could not lock %s\n%s", fcState.getPath(), e.getMessage());
        }
        return fcState;
    }

    public static FcState lock(Path filePath) {
        var fcState = FcState.from(filePath);
        return lock(fcState);
    }

    public static FcState unlock(FcState fcState) {
        return unlock(fcState.getPath(), fcState.getFc());
    }

    public static FcState unlock(Path filePath, FileChannel fc) {
        try {
            fc.close();
            return new FcState(filePath, fc, false);
        } catch (IOException | OverlappingFileLockException e) {
            LogU.warnPlain("Could not unlock %s", fc);
            return new FcState(filePath, fc, true);
        }
    }
}
