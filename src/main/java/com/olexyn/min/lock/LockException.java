package com.olexyn.min.lock;

import java.io.Serial;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LockException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public LockException(String message) {
		super(message);
	}
}
