package com.kthisiscvpv.util.logger;

public abstract class AbstractLogger {

	public static enum Level {

		SYSTEM(false), INFO(true), WARN(false), ERROR(false), FATAL(false);

		private boolean verbose;

		Level(boolean verbose) {
			this.verbose = verbose;
		}

		public boolean isVerbose() {
			return this.verbose;
		}
	}

	public abstract void println(Class<?> c, Level l);

	public abstract void println(Class<?> c, Level l, String x);

	public abstract void print(Class<?> c, Level l, String x);

}
