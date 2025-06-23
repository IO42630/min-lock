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
        unlock(fcState.getFc());
        return FcState.from(fcState.getPath());
    }

    public static void unlock(FileChannel fc) {
        try {
            fc.close();
        } catch (IOException | OverlappingFileLockException e) {
            LogU.warnPlain("Could not unlock %s", fc);
        }
    }
}
