package com.olexyn.min.lock;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;

import com.olexyn.min.log.LogU;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

@Data
@AllArgsConstructor
public class FcState {


	private @NonNull Path path;
	private @NonNull FileChannel fc;
    private boolean locked;

	public boolean isUnlocked() {
        return !isLocked();
    }

	public File getFile() {
		return path.toFile();
	}

	public boolean isFileExists() {
		return getFile().exists();
	}

	public static FcState from(Path filePath) {
		try {
			var fc = FileChannel.open(filePath, READ, WRITE);
			return new FcState(filePath, fc, false);
		} catch (IOException | OverlappingFileLockException e) {
			LogU.warnPlain("Could not open %s\n%s", filePath, e.getMessage());
			throw new LockException(e.getMessage());
		}
	}

}
