package com.studentnow.android.service;

import java.io.Serializable;

public class NotificationPersistance implements Serializable {

	private static final long serialVersionUID = -4559298604288411724L;

	protected long lastMs = System.currentTimeMillis();

}
