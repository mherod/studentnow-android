package org.herod.studentnow.service;

import herod.gd.Directions;
import herod.gd.DirectionsQueryURL;
import herod.gd.Leg;
import herod.gd.Route;

import org.herod.studentnow.service.LocationCache.CachedLocation;
import org.studentnow.Session;
import org.studentnow._;

import android.util.Log;

public class TravelModule implements ServiceModule {

	private final LiveService liveService;

	private boolean requestTravelUpdate = false;

	public TravelModule(LiveService liveService) {
		this.liveService = liveService;
	}

	@Override
	public void load() {
	}

	@Override
	public void schedule() {
		requestUpdate();

	}

	@Override
	public void cancel() {
	}

	public void requestUpdate() {
		requestTravelUpdate = true;
	}

	private LocationModule getLocationModule() {
		return ((LocationModule) liveService
				.getServiceModule(LocationModule.class));
	}

	@Override
	public void cycle() {

		if (requestTravelUpdate) {
			requestTravelUpdate = false;

			CachedLocation loc = getLocationModule().getLocationCache()
					.getLastLocation();

			Session nextSession = liveService.getTimetable().getNextSession();

			DirectionsQueryURL directionURL = null;
			if (nextSession != null
					&& nextSession.isSet(_.FIELD_ROOM_LAT, _.FIELD_ROOM_LON)) {

				String start = loc.getLatitude() + "," + loc.getLongitude();

				String end = nextSession.get(_.FIELD_ROOM_LAT) + ","
						+ nextSession.get(_.FIELD_ROOM_LON);

				Log.d("ss", "searching start " + start + " to " + end);
				directionURL = new DirectionsQueryURL(start, end, true);
			} else {
				Log.d("ss", "No location");
			}
			if (directionURL != null) {
				// directionURL.addParam("mode", "transit");
				long a = nextSession.getStartTime().getTimeAsDate().getTime();
				// directionURL.addParam("arrival_time", String.valueOf(a));
				try {
					Directions directions = Directions.fetch(directionURL);
					Route route = directions.getRoutes().get(0);
					for (Leg leg : route.getLegs()) {
						long durationSecs = leg.getDurationSecs();
						nextSession.set(_.FIELD_TRAVEL_DURATION,
								String.valueOf(durationSecs));
						Log.d("loc", "Duration: " + leg.getDurationSecs());
						Log.d("loc", "Depart: " + leg.getStartAddress());
						Log.d("loc", "Arrive: " + leg.getEndAddress());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			((NotificationModule) liveService
					.getServiceModule(NotificationModule.class))
					.requestCardsRefresh();

		}

	}

	@Override
	public boolean save() {
		return true;
	}
}
