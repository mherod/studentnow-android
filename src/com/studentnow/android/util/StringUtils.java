package com.studentnow.android.util;

import java.util.Locale;

public class StringUtils {

	public static String capitalize(String s, boolean allWords) {
		if (!allWords) {
			return capitalize(s);
		}
		String[] words = s.split(" ");
		String out = "";
		for (int i = 0; i < words.length; i++) {
			out += s.concat(words[i]);
			if (i != words.length - 1)
				out += s.concat(" ");
		}
		return out;
	}

	public static String capitalize(String s) {
		Locale l = Locale.getDefault();
		if (s.length() > 1) {
			return s.toUpperCase(l).substring(0, 1) + s.substring(1);
		}
		return s.toUpperCase(l);
	}

}
