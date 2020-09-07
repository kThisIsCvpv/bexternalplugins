package com.kthisiscvpv.util;

public class Utils {

	public static int parseIntExitIfFail(String s) {
		if (s == null)
			s = "null";

		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			System.err.printf("Error: %s is not a valid integer.\n", s);
			System.exit(1);
			return -1;
		}
	}
}
