package com.alterronix.alteronixback.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidResourceException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	private String statusCode;
	
	public InvalidResourceException(String message) {
		super(message);
	}

	public InvalidResourceException(String message, String statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

}
