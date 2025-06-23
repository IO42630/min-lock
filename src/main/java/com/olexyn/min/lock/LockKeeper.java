package com.olexyn.min.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.olexyn.min.log.LogU;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LockKeeper {


    private static final Map<Path, FcState> LOCKS = new HashMap<>();

    public static boolean lockDir(Path dirPath) {
        List<FcState> fcStates;
        try {
            fcStates = Files.walk(dirPath)
                .filter(filePath -> filePath.toFile().isFile())
                .map(LockUtil::lock)
                .toList();
        } catch (IOException e) {
            return false;
        }
        LogU.infoPlain("LOCKED " + fcStates.size() + " files in " + dirPath);
        fcStates.forEach(fcState -> LOCKS.put(fcState.getPath(), fcState));
        return fcStates.stream().noneMatch(FcState::isUnlocked);
    }



    public static void unlockAll() {
        LogU.infoPlain("UNLOCKING ALL.");
            LOCKS.values().forEach(
                fcState -> LockUtil.unlock(fcState.getPath(), fcState.getFc())
            );
    }

    public static FileChannel getFc(Path path) {
        var fcState = LOCKS.get(path);
        if (fcState != null && fcState.getFc().isOpen()) {
            return fcState.getFc();
        }
        if (!path.toFile().exists()) {
            fcState = FcState.from(path);
        } else {
            fcState = LockUtil.lock(path);
        }
        LOCKS.put(path, fcState);
        return fcState.getFc();
    }

}
