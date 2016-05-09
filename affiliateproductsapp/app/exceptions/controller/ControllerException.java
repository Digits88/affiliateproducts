package exceptions.controller;

import exceptions.BaseException;

public class ControllerException extends BaseException {

	public ControllerException(String message) {
		super(message);
	}

	public ControllerException(String message, Object... params) {
		super(message, params);
	}
}
