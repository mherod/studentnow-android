package com.studentnow.android;

import java.util.ArrayList;
import java.util.List;

import org.studentnow.Course;
import org.studentnow.Module;
import org.studentnow.Session;
import org.studentnow.Timetable;
import org.studentnow.api.CacheAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CatalogueCache extends SQLiteOpenHelper implements
		CacheAdapter {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "cache";

	private static final String TABLE_COURSES = "courses",
			TABLE_MODULES = "modules", TABLE_SESSIONS = "sessions",
			TABLE_TIMETABLE = "timetable";

	private static final String FIELD_PROGID = "programme_id",
			FIELD_COURSE_NAME = "course_name",
			FIELD_COURSE_CODE = "course_code",
			FIELD_MODULE_NAME = "module_name";

	public CatalogueCache(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + TABLE_COURSES + "("

		+ FIELD_PROGID + " TEXT PRIMARY KEY," + FIELD_COURSE_NAME + " TEXT,"
				+ FIELD_COURSE_CODE + " TEXT"

				+ ")");

		db.execSQL("CREATE TABLE " + TABLE_MODULES + "("

		+ FIELD_MODULE_NAME + " TEXT PRIMARY KEY"

		+ ")");

		db.execSQL("CREATE TABLE " + TABLE_SESSIONS + "("

		+ "session_code TEXT PRIMARY KEY," + "session_module TEXT,"
				+ "session_type TEXT," + "session_tutor TEXT,"
				+ "session_weeks TEXT," + "session_day TEXT,"
				+ "session_time TEXT," + "session_length TEXT,"
				+ "session_room_name TEXT," + "session_room_code TEXT,"
				+ "session_size INTEGER," + "session_room_size INTEGER"

				+ ")");

		db.execSQL("CREATE TABLE " + TABLE_TIMETABLE + "("

		+ "programme_id TEXT PRIMARY KEY," + "session_code TEXT"

		+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);

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

		String countQuery = "SELECT  * FROM " + TABLE_COURSES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		return cursor.getCount();

	}

	@Override
	public void empty() {

		onUpgrade(this.getWritableDatabase(), DATABASE_VERSION,
				DATABASE_VERSION);

	}

	@Override
	public Course getCourse(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Course> getCourses() {

		List<Course> courses = new ArrayList<Course>();

		String selectQuery = "SELECT  * FROM " + TABLE_COURSES;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {

			do {

				String programme_id = cursor.getString(0);
				String course_name = cursor.getString(1);
				// String course_code = cursor.getString(2);

				courses.add(new Course(programme_id, course_name));

			} while (cursor.moveToNext());

		}

		return courses;

	}

	@Override
	public List<Module> getModules() {

		// TODO

		List<Module> modules = new ArrayList<Module>();

		String selectQuery = "SELECT  * FROM " + TABLE_MODULES;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {

			do {

				// String module_name = cursor.getString(0);

				// cat.module(module_name);

			} while (cursor.moveToNext());

		}

		return modules;

	}

	@Override
	public List<Session> getSessions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Timetable> getTimetables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveCourse(Course c) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_PROGID, c.getProgrammeID());
		values.put(FIELD_COURSE_NAME, c.getName());
		values.put(FIELD_COURSE_CODE, c.getCode());

		db.insert(TABLE_COURSES, null, values);
		db.close();

	}

	@Override
	public void saveCourses(List<Course> cs) {

		SQLiteDatabase db = this.getWritableDatabase();

		for (Course c : cs) {

			ContentValues values = new ContentValues();
			values.put(FIELD_PROGID, c.getProgrammeID());
			values.put(FIELD_COURSE_NAME, c.getName());
			values.put(FIELD_COURSE_CODE, c.getCode());

			db.insert(TABLE_COURSES, null, values);

		}

		db.close();

	}

	@Override
	public void saveModule(Module m) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_MODULE_NAME, m.getName());

		db.insert(TABLE_MODULES, null, values);
		db.close();

	}

	@Override
	public void saveSession(Session s) {

	}

	@Override
	public void saveTimetable(Timetable arg0) {
		// TODO Auto-generated method stub

	}

}
