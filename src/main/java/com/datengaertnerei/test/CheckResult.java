package com.datengaertnerei.test;

public class CheckResult {

	private boolean isError;
	private String selector;
	private String message;

	public CheckResult(boolean isError, String selector, String message) {
		this.isError = isError;
		this.selector = selector;
		this.message = message;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
