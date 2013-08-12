package org.herod.studentnow.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class LocationCache extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "location_cache";

	private static final String TABLE_FIXES = "fixes";

	private static final String FIELD_FIX_TIME = "time";
	private static final String FIELD_FIX_PRVID = "provider";
	private static final String FIELD_FIX_LAT = "lat";
	private static final String FIELD_FIX_LNG = "lng";

	public LocationCache(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + TABLE_FIXES

		+ "("

		+ FIELD_FIX_TIME + " LONG PRIMARY KEY,"

		+ FIELD_FIX_PRVID + " TEXT,"

		+ FIELD_FIX_LAT + " DOUBLE,"

		+ FIELD_FIX_LNG + " DOUBLE" +

		")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FIXES);
		onCreate(db);
	}

	/*
	 * 
	 * // Getting single contact Contact getContact(int id) { SQLiteDatabase db
	 * = this.getReadableDatabase();
	 * 
	 * Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID, KEY_NAME,
	 * KEY_PH_NO }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null,
	 * null, null, null); if (cursor != null) cursor.moveToFirst();
	 * 
	 * Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
	 * cursor.getString(1), cursor.getString(2)); // return contact return
	 * contact; }
	 * 
	 * // Getting All Contacts public List<Contact> getAllContacts() {
	 * List<Contact> contactList = new ArrayList<Contact>(); // Select All Query
	 * String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
	 * 
	 * SQLiteDatabase db = this.getWritableDatabase(); Cursor cursor =
	 * db.rawQuery(selectQuery, null);
	 * 
	 * // looping through all rows and adding to list if (cursor.moveToFirst())
	 * { do { Contact contact = new Contact();
	 * contact.setID(Integer.parseInt(cursor.getString(0)));
	 * contact.setName(cursor.getString(1));
	 * contact.setPhoneNumber(cursor.getString(2)); // Adding contact to list
	 * contactList.add(contact); } while (cursor.moveToNext()); }
	 * 
	 * // return contact list return contactList; }
	 * 
	 * // Updating single contact public int updateContact(Contact contact) {
	 * SQLiteDatabase db = this.getWritableDatabase();
	 * 
	 * ContentValues values = new ContentValues(); values.put(KEY_NAME,
	 * contact.getName()); values.put(KEY_PH_NO, contact.getPhoneNumber());
	 * 
	 * // updating row return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
	 * new String[] { String.valueOf(contact.getID()) }); }
	 * 
	 * // Deleting single contact public void deleteContact(Contact contact) {
	 * SQLiteDatabase db = this.getWritableDatabase(); db.delete(TABLE_CONTACTS,
	 * KEY_ID + " = ?", new String[] { String.valueOf(contact.getID()) });
	 * db.close(); }
	 */

	public int getCoursesCount() {

		String countQuery = "SELECT  * FROM " + TABLE_FIXES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		return cursor.getCount();

	}

	public void empty() {
		onUpgrade(this.getWritableDatabase(), DATABASE_VERSION,
				DATABASE_VERSION);

	}

	public CachedLocation getLastLocation() {

		CachedLocation loc = new CachedLocation();

		String selectQuery = "SELECT  * "

		+ "FROM " + TABLE_FIXES + " "

		+ "ORDER BY " + FIELD_FIX_TIME + " "

		+ "DESC LIMIT 1";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			loc.setTime(cursor.getLong(0));
			loc.setProvider(cursor.getString(1));
			loc.setLat(cursor.getDouble(2));
			loc.setLng(cursor.getDouble(3));
			return loc;
		}
		return null;
	}

	public void storeLocation(Location loc) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_FIX_TIME, loc.getTime());
		values.put(FIELD_FIX_PRVID, loc.getProvider());
		values.put(FIELD_FIX_LAT, loc.getLatitude());
		values.put(FIELD_FIX_LNG, loc.getLongitude());

		db.insert(TABLE_FIXES, null, values);
		db.close();

	}

	public class CachedLocation {

		private long time = 0;
		private String provider = "";
		private double lat = 0.0, lng = 0.0;

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public String getProvider() {
			return provider;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public double getLongitude() {
			return lng;
		}

		public void setLng(double lng) {
			this.lng = lng;
		}

		public double getLatitude() {
			return lat;
		}

		public void setLat(double lat) {
			this.lat = lat;
		}

	}

}
