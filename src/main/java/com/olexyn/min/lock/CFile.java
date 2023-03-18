package com.olexyn.min.lock;

import java.io.File;
import java.io.Serial;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class CFile extends File {

    @Serial
    private static final long serialVersionUID = -6865384841588563062L;

    private final Path path;
    private final FileChannel fc;
    private final boolean locked;

    public CFile(Path path, FileChannel fc, boolean locked) {
        super(path.toFile().getPath());
        this.path = path;
        this.fc = fc;
        this.locked = locked;
    }

    public FileChannel getFc() {
        return fc;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isUnlocked() {
        return !isLocked();
    }

    @Override
    public Path toPath() {
        return path;
    }

    @Override
    public String toString() {
        String lockState = isLocked() ? "(locked)" : "(unlocked)";
        return toPath().toAbsolutePath() + " " + lockState;
    }

}
