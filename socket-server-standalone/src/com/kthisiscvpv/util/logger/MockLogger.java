package com.kthisiscvpv.util.logger;

import java.io.PrintStream;
import java.text.SimpleDateFormat;

public class MockLogger extends AbstractLogger {

	private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private PrintStream stream;

	public MockLogger(PrintStream stream) {
		this.stream = stream;
	}

	@Override
	public void println(Class<?> c, Level l) {
		this.println(c, l, "");
	}

	@Override
	public void println(Class<?> c, Level l, String x) {
		this.print(c, l, x + "\n");
	}

	@Override
	public void print(Class<?> c, Level l, String x) {
		this.stream.printf("%s [%s] %s - %s", TIME_FORMAT.format(System.currentTimeMillis()), l.name(), c.getName(), x);
	}
}
