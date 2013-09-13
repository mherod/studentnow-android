package com.studentnow.android;

import android.content.Context;
import android.content.Intent;

public class __ {

	public static final String File_Settings = "settings";

	public static final String Key_Course = "Course";

	public static final String Save_CourseName = "CourseName";
	public static final String Save_CourseProgID = "CourseProgramme";

	public static final String INTENT_CARD_UPDATE = "com.studentnow.android.CARD_UPDATE";
	public static final String INTENT_NOTIFICATION = "com.studentnow.android.NOTIFICATION";
	public static final String INTENT_UPDATE_CARDS = "com.studentnow.android.UPDATE_CARDS";
	public static final String INTENT_POLL_LOC = "com.studentnow.android.POLL_LOCATION";
	public static final String INTENT_CONNECT_SERVICE = "com.studentnow.android.CONNECT_SERVICE";
	public static final String INTENT_CLOSE_APP = "com.studentnow.android.CLOSE_APP";

	public static final String DISPLAY_MESSAGE_ACTION = "com.studentnow.android.DISPLAY_MESSAGE";

	public static final String EXTRA_MESSAGE = "message";

	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}

}
