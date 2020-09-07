package com.kthisiscvpv.util.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class MockFileLogger extends AbstractLogger {

	private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat FILE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private boolean verbose;
	private PrintStream stream;

	private String currentFile;

	public MockFileLogger(PrintStream stream) {
		this(stream, false);
	}

	public MockFileLogger(PrintStream stream, boolean verbose) {
		this.stream = stream;
		this.verbose = verbose;
		this.currentFile = this.getFileName();
	}

	private String getFileName() {
		return FILE_TIME_FORMAT.format(System.currentTimeMillis()) + ".txt";
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
		if (l.isVerbose() && !this.verbose)
			return;

		this.stream.printf("%s [%s] %s - %s", TIME_FORMAT.format(System.currentTimeMillis()), l.name(), c.getName(), x);

		synchronized (this.currentFile) {
			try {
				File outputFile = new File(this.getFileName());

				FileWriter fr = new FileWriter(outputFile, true);
				BufferedWriter br = new BufferedWriter(fr);
				PrintWriter pr = new PrintWriter(br);

				pr.printf("%s [%s] %s - %s", TIME_FORMAT.format(System.currentTimeMillis()), l.name(), c.getSimpleName(), x);

				pr.close();
				br.close();
				fr.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
