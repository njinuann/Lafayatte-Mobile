package org.redlamp.beans;

public class CounterSingleton {

	private static CounterSingleton instance = new CounterSingleton();

	private Integer counter = 0;

	private CounterSingleton() {
		counter = 0;
	}

	public static CounterSingleton getInstance() {
		return instance;
	}

	public synchronized void increment() {
		counter++;
	}

	public Integer getCounter() {
		return counter;
	}

	public synchronized void reset() {
		counter = 0;
	}

}