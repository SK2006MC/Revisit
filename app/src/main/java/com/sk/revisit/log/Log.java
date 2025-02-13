package com.sk.revisit.log;

import java.util.ArrayList;
import java.util.List;

public class Log {

	static ArrayList<String[]> logs = new ArrayList<>();

	public static void e(String tag, String msg, Exception e) {
		logs.add(new String[]{tag, msg, e.getMessage()});
	}

	public static void e(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void d(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void i(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void v(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void w(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void wtf(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static List<String[]> getLogs() {
		return new ArrayList<>(logs);
	}
}