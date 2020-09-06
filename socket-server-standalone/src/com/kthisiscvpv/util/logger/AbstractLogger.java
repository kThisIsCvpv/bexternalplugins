package com.kthisiscvpv.util.logger;

public abstract class AbstractLogger {

	public static enum Level {
		INFO, WARN, ERROR, FATAL;
	}

	public abstract void println(Class<?> c, Level l);

	public abstract void println(Class<?> c, Level l, String x);

	public abstract void print(Class<?> c, Level l, String x);

}
