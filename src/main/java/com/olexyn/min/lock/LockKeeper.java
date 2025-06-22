package com.olexyn.min.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.olexyn.min.log.LogU;

public class LockKeeper {

    private static final int TRY_COUNT = 4;

    private final static Map<Path, FcState> LOCKS = new HashMap<>();

    public static boolean lockDir(Path dirPath) {
        List<FcState> fcStates;
        try {
            fcStates = Files.walk(dirPath)
                .filter(filePath -> filePath.toFile().isFile())
                .map(filePath -> LockUtil.lockFile(filePath, TRY_COUNT))
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
                fcState -> LockUtil.unlockFile(fcState.getPath(), fcState.getFc(), 4)
            );
    }

    public static FileChannel getFc(Path path) {
        var fcState = LOCKS.get(path);
        if (fcState != null && fcState.getFc() != null && fcState.getFc().isOpen()) {
            return fcState.getFc();
        }
        if (!path.toFile().exists()) {
            fcState = LockUtil.newFile(path);
        } else {
            fcState = LockUtil.lockFile(path);
        }
        LOCKS.put(path, fcState);
        return fcState.getFc();
    }

}
