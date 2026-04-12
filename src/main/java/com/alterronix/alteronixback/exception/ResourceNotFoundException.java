package com.alterronix.alteronixback.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceNotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	private String statusCode;

	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException(String message, String statusCode) {
		super(message);
		this.statusCode = statusCode;
	}
}
