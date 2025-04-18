package com.sk.revisit.log;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Log {

	static ArrayList<String[]> logs = new ArrayList<>();

	public static void e(String tag, String msg, @NonNull Exception e) {
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

	public static void saveLog(File path) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		for (String[] log : logs) {
			writer.write(Arrays.toString(log));
			writer.newLine();
			writer.flush();
		}
		writer.close();
	}

}