package com.kthisiscvpv.util.logger;

import com.kthisiscvpv.util.logger.MockLogger.Level;

public abstract class AbstractLogger {

	public abstract void println(Class<?> c, Level l);

	public abstract void println(Class<?> c, Level l, String x);

	public abstract void print(Class<?> c, Level l, String x);

}
