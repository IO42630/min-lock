package com.olexyn.min.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.olexyn.min.log.LogU;

public final class LockKeeper {

    private static final int TRY_COUNT = 4;

    private static final Map<Path, CFile> LOCKS = new HashMap<>();

    private LockKeeper() {
    }

    public static boolean lockDir(Path dirPath) {
        List<CFile> fcStates;
        try {
            fcStates = Files.walk(dirPath)
                    .filter(filePath -> filePath.toFile().isFile())
                    .map(filePath -> LockUtil.lockFile(filePath, TRY_COUNT))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return false;
        }
        LogU.infoPlain("LOCKED " + fcStates.size() + " files in " + dirPath);
        fcStates.forEach(fcState -> LOCKS.put(fcState.toPath(), fcState));
        return fcStates.stream().noneMatch(CFile::isUnlocked);
    }

    public static void unlockAll() {
        LogU.infoPlain("UNLOCKING ALL.");
        LOCKS.values().forEach(
                fcState -> LockUtil.unlockFile(fcState.toPath(), fcState.getFc(), 4)
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
