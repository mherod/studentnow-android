package com.studentnow.android;

import android.content.Context;
import android.content.Intent;

public class __ {

	public static final String File_Settings = "settings";

	public static final String Key_Course = "Course";

	public static final String Save_CourseName = "CourseName";
	public static final String Save_CourseProgID = "CourseProgramme";

	public static final String PACKAGE = "com.studentnow.android";

	public static final String INTENT_CARD_UPDATE = PACKAGE + ".CARD_UPDATE";
	public static final String INTENT_NOTIFICATION = PACKAGE + ".NOTIFICATION";
	public static final String INTENT_UPDATE_CARDS = PACKAGE + ".UPDATE_CARDS";
	public static final String INTENT_POLL_LOC = PACKAGE + ".POLL_LOCATION";
	public static final String INTENT_CONNECT_SERVICE = PACKAGE + ".CONNECT_SERVICE";
	public static final String INTENT_CLOSE_APP = PACKAGE + ".CLOSE_APP";
	public static final String INTENT_ALERT = PACKAGE + ".ALERT_ACTIVITY";

	public static final String DISPLAY_MESSAGE_ACTION = "com.studentnow.android.DISPLAY_MESSAGE";

	public static final String EXTRA_MESSAGE = "message";

	public static final String EXTRA_ALERT = "alert_string";

	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}

}
