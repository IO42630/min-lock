package com.olexyn.min.lock;

import java.nio.channels.FileChannel;
import java.nio.file.Path;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FcState {


	private Path path;
	private FileChannel fc;
    private boolean locked;

	public boolean isUnlocked() {
        return !isLocked();
    }

}
