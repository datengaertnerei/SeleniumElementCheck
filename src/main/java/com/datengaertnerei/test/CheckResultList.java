package com.datengaertnerei.test;

import java.util.Collection;
import java.util.LinkedList;

public class CheckResultList<E extends CheckResult> extends LinkedList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5884643747943698812L;
	private int errorCount = 0;

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(CheckResult result) {

		if (result.isError()) {
			errorCount++;
		}
		return super.add((E) result);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		errorCount += ((CheckResultList<? extends E>) c).getErrorCount();

		return super.addAll(c);
	}

	public int getErrorCount() {
		return errorCount;
	}

}
