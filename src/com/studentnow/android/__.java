package com.studentnow.android;

import android.content.Context;
import android.content.Intent;

public class __ {

	public static final String File_Settings = "settings";

	public static final String Key_Course = "Course";

	public static final String Save_CourseName = "CourseName";
	public static final String Save_CourseProgID = "CourseProgramme";

	public static final String Tag_CardActivity = "CardActivity";

	public static final String Intent_CardUpdate = "com.studentnow.android.CARD_UPDATE";
	public static final String Intent_Notification = "studentnow.notification";
	public static final String Intent_ProgrammeUpdate = "studentnow.programmeupdate";
	public static final String Intent_HomeLocPoll = "studentnow.homelocpoll";
	public static final String Intent_ConnectService = "com.studentnow.android.CONNECT_SERVICE";
	public static final String Intent_CloseApp = "com.studentnow.android.CLOSE_APP";

	public static final String DISPLAY_MESSAGE_ACTION = "com.studentnow.android.DISPLAY_MESSAGE";

	public static final String EXTRA_MESSAGE = "message";

	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}

}
