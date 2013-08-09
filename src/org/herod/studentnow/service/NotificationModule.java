package org.herod.studentnow.service;

import org.herod.studentnow.__;

import android.content.Intent;

public class NotificationModule implements ServiceModule {

	private LiveService liveService = null;

	private boolean requestCardRefresh = false;

	public NotificationModule(LiveService liveService) {
		this.liveService = liveService;
	}

	@Override
	public void load() {

	}

	@Override
	public void schedule() {

	}

	@Override
	public void cancel() {

		
	}

	@Override
	public void cycle() {
		if (requestCardRefresh) {
			requestCardRefresh = false;

			Intent updateCardsIntent = new Intent(__.Intent_CardUpdate);
			liveService.sendBroadcast(updateCardsIntent);
		}
	}

	public void requestCardsRefresh() {
		requestCardRefresh = true;
	}

}
