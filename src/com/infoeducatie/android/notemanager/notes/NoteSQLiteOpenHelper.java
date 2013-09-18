package com.infoeducatie.android.notemanager.notes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteSQLiteOpenHelper extends SQLiteOpenHelper {
	
	public final static int VERSION = 1;
	public final static String DB_NAME = "notemanager_db.sqlite";
	
	public final static String NOTES_TABLE = "NotesTable";
	public final static String PASSWORDS_TABLE = "PasswordsTable";
	public final static String LOCATIONS_TABLE = "LocationsTable";
	public final static String TWEET_TABLE = "TweetTable";
	
	public final static String NOTE_ID = "Id";
	public final static String NOTE_TITLE = "Title";
	public final static String NOTE_CONTENT = "Content";
	public final static String NOTE_DATE = "Date";
	public final static String NOTE_HASPASSWORD = "HasPassword";
	public final static String NOTE_HASLOCATION = "HasLocation";
	public final static String NOTE_HASTWEET = "HasTweet";
	public final static String NOTE_COMPLETE = "IsComplete";
	
	public final static String NOTE_PASSWORD = "Password";
	public final static String NOTE_LATITUDE = "Latitude";
	public final static String NOTE_LONGITUDE = "Longitude";
	public final static String NOTE_ADDRESS = "Address";
	public final static String NOTE_TWEET = "Tweet";

	public NoteSQLiteOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	private void createTables(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + NOTES_TABLE + " (" 
				+ NOTE_ID + " integer primary key autoincrement not null, "
				+ NOTE_TITLE + " text, "
				+ NOTE_CONTENT + " text, "
				+ NOTE_DATE + " integer, "
				+ NOTE_HASPASSWORD + " text, "
				+ NOTE_HASLOCATION + " text, "
				+ NOTE_HASTWEET + " text, "
				+ NOTE_COMPLETE + " text ); ");
		db.execSQL("CREATE TABLE " + PASSWORDS_TABLE + " (" 
				+ NOTE_ID + " integer, "
				+ NOTE_PASSWORD + " text ); " );
		db.execSQL("CREATE TABLE " + LOCATIONS_TABLE + " (" 
				+ NOTE_ID + " integer, "
				+ NOTE_LATITUDE + " real, " 
				+ NOTE_LONGITUDE + " real, "
				+ NOTE_ADDRESS + " text ); ");
		db.execSQL("CREATE TABLE " + TWEET_TABLE + " (" 
				+ NOTE_ID + " integer, "
				+ NOTE_TWEET+ " text ); " );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
