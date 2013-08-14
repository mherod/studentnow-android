package com.studentnow.android.service;

import herod.gd.Directions;
import herod.gd.DirectionsQueryURL;
import herod.gd.DistanceExtra;
import herod.gd.Leg;
import herod.gd.Route;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.studentnow.Session;
import org.studentnow._;

import android.util.Log;

import com.studentnow.android.ObjectFiles;
import com.studentnow.android.service.LocationCache.CachedLocation;

public class TravelModule implements ServiceModule {

	private final LiveService liveService;

	private boolean requestTravelUpdate = false;

	private TravelInformation travelInformation = null;
	private final String travelInformationFile = "travelinfo.dat";

	public TravelModule(LiveService liveService) {
		this.liveService = liveService;
	}

	@Override
	public void load() {
		try {
			travelInformation = (TravelInformation) ObjectFiles
					.readObject(getFolder() + travelInformationFile);
			Log.d("ssssss", "reeeed travel ifno");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean save() {
		if (travelInformation != null) {
			try {
				ObjectFiles.saveObject(travelInformation, getFolder()
						+ travelInformationFile);
			} catch (IOException e) {
				return false;
			}
		}
		return true;
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

	@Override
	public void cycle() {
		if (travelInformation == null || requestTravelUpdate) {
			requestTravelUpdate = false;

			CachedLocation loc = getLastLocation();

			DirectionsQueryURL directionURL = null;
			float meters = 0;

			Session nextSession = liveService.getTimetable() == null ? null
					: liveService.getTimetable().getNextSession();
			long nextDate = nextSession == null ? 0 : nextSession.getNextDate();

			if (loc != null && nextSession != null
					&& nextSession.isSet(_.FIELD_ROOM_LAT, _.FIELD_ROOM_LON)) {

				meters = DistanceExtra.distMeters(loc.getLatitude(),
						loc.getLongitude(),
						nextSession.getDouble(_.FIELD_ROOM_LAT),
						nextSession.getDouble(_.FIELD_ROOM_LON));

				if (meters > 500) {

					String start = loc.getLatitude() + "," + loc.getLongitude();
					String end = nextSession.get(_.FIELD_ROOM_LAT) + ","
							+ nextSession.get(_.FIELD_ROOM_LON);

					Log.d("ss", "searching start " + start + " to " + end);
					directionURL = new DirectionsQueryURL(start, end, true);

				}
			}
			if (directionURL != null) {
				// directionURL.addParam("mode", "transit");
				// directionURL.addParam("arrival_time", String.valueOf(a));
				try {
					Directions directions = Directions.fetch(directionURL);
					Route route = directions.getRoutes().get(0);

					Log.d("ssssss", "got directions");

					for (Leg leg : route.getLegs()) {
						if (leg == null) {
							continue;
						}
						try {
							travelInformation = new TravelInformation(leg);
							Log.d("ssssss", "travel ifno");
							break;
						} catch (NullPointerException npe) {
							npe.printStackTrace();
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}		
				
			}
			if (travelInformation != null) {
				((NotificationModule) liveService
						.getServiceModule(NotificationModule.class))
						.requestCardsRefresh();	
			}
		}

	}

	private String getFolder() {
		return liveService.getFilesDir() + File.separator;
	}

	private LocationModule getLocationModule() {
		return ((LocationModule) liveService
				.getServiceModule(LocationModule.class));
	}

	private CachedLocation getLastLocation() {
		try {
			return getLocationModule().getLocationCache().getLastLocation();
		} catch (Exception e) {
			return null;
		}
	}

	public TravelInformation getTravelInformation() {
		return travelInformation;
	}

	public class TravelInformation implements Serializable {

		private static final long serialVersionUID = 1L;

		private final long durationSecs;
		private String departTime = "0:00";
		private String arriveTime = "0:00";
		private final String startAddress;
		private final String endAddress;
		private final String endAddressShort;

		public TravelInformation(Leg l) {
			durationSecs = l.getDurationSecs();
			//departTime = l.getDepartureTime().getText();
			//arriveTime = l.getArrivalTime().getText();
			startAddress = l.getStartAddress();
			endAddress = l.getEndAddress();
			endAddressShort = endAddress.split(",")[0];
		}

		public long getDurationMins() {
			return durationSecs / 60;
		}

		public long getDurationSecs() {
			return durationSecs;
		}

		public String getDepartTime() {
			return departTime;
		}

		public String getArriveTime() {
			return arriveTime;
		}

		public String getStartAddress() {
			return startAddress;
		}

		public String getEndAddress() {
			return endAddress;
		}

		public String getEndAddressShort() {
			return endAddressShort;
		}

	}

}
