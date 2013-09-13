package com.studentnow.android.util;

public class SuppressionPeriod {

	public static final long SUPPRESS_MIN = 5 * 1000;
	public static final long SUPPRESS_MAX = 2 * 60 * 1000;

	private long suppressUpdate, suppressUpdateBackoff;

	public SuppressionPeriod() {
		reset();
	}

	public long suppress() {
		if (suppressUpdate > 0 && (suppressUpdateBackoff *= 2) > SUPPRESS_MAX) {
			suppressUpdateBackoff = SUPPRESS_MAX;
		}
		suppressUpdate = System.currentTimeMillis() + suppressUpdateBackoff;
		return suppressUpdateBackoff;
	}

	public boolean isSuppressed() {
		return System.currentTimeMillis() < suppressUpdate;
	}

	public void reset() {
		suppressUpdate = 0;
		suppressUpdateBackoff = SUPPRESS_MIN;
	}

}
