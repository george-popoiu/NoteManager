package com.infoeducatie.android.notemanager;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.infoeducatie.android.notemanager.adapters.NoteListAdapter;
import com.infoeducatie.android.notemanager.notes.Note;
import com.infoeducatie.android.notemanager.notes.NoteSQLiteOpenHelper;
import static com.infoeducatie.android.notemanager.notes.NoteSQLiteOpenHelper.*;

public class NoteManagerApplication extends Application implements OnSharedPreferenceChangeListener {
	
	public final static String DEBUG = "DEBUG";
	public final static String TWITTER_USERNAME_KEY = "twitter_username";
	public final static String TWITTER_PASSWORD_KEY = "twitter_username";
	public final static String USE_LOCATION_KEY = "use_location";
	public final static String NOTIFICATION_DISTANCE_KEY = "notification_distance";
	public static final String USE_LOCATION_SLEEP_KEY = "location_during_sleep";
	public static final String USE_NOTIFICATION = "use_notification";
	public final static String AUTH_KEY = "auth_key";
	public final static String AUTH_SECRET_KEY = "auth_secret_key";

	private SQLiteDatabase db;
	private SharedPreferences prefs;
	
	String twitterUsername, twitterPassword;
	boolean useLocation;
	int notificationDistance;
	boolean useLocationDuringSleep;
	
	ArrayList<Note> currentNotes;
	private NoteListAdapter adapter;
	private boolean useNotification;
	private String token;
	private String tokenSecret;
	private OAuthHelper oAuthHelper;
	private Twitter twitter;
	private RequestToken currentRequestToken;

	@Override
	public void onCreate() {
		super.onCreate();
		
		NoteSQLiteOpenHelper helper = new NoteSQLiteOpenHelper(this);
		db = helper.getWritableDatabase();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		loadPrefs();
		
		loadNotes();
		
		oAuthHelper = new OAuthHelper(this, this);
		twitter = new TwitterFactory().getInstance();
		oAuthHelper.configureOAuth(twitter);
	}
	
	class LocationData {
		public String addressLine;
		public double latitude, longitude;
		public LocationData(String addressLine, double latitude, double longitude) {
			super();
			this.addressLine = addressLine;
			this.latitude = latitude;
			this.longitude = longitude;
		}
		public LocationData() { }
	}

	private void loadNotes() {
		currentNotes = new ArrayList<Note>();
		Cursor cursor = db.query(NOTES_TABLE, new String[]{ NOTE_ID, NOTE_TITLE, NOTE_CONTENT, NOTE_DATE, NOTE_HASPASSWORD, 
															NOTE_HASLOCATION, NOTE_HASTWEET, NOTE_COMPLETE }, 
						null, null, null, null, String.format("%s,%s", NOTE_COMPLETE, NOTE_TITLE));
		cursor.moveToFirst();

		Note note;
		if( !cursor.isAfterLast() ) {
			do {
				long id = cursor.getLong(0);
				String title = cursor.getString(1);
				String content = cursor.getString(2);
				long date = cursor.getLong(3);
				boolean hasPassword = Boolean.parseBoolean(cursor.getString(4));
				boolean hasLocation = Boolean.parseBoolean(cursor.getString(5));
				boolean hasTweet = Boolean.parseBoolean(cursor.getString(6));
				boolean complete = Boolean.parseBoolean(cursor.getString(7));
				
				note = new Note();
				note.setId(id); note.setTitle(title); note.setContent(content); note.setCreationDateToMillis(date); 
				note.setHasPassword(hasPassword); note.setHasLocation(hasLocation); note.setHasTweet(hasTweet); note.setComplete(complete);
				
				if(hasPassword) {
					String password = loadPassword(id);
					if( !password.equals("") ) note.setPassword(password);
				}
				if(hasLocation) {
					LocationData locationData = loadLocation(id);
					if(null!=locationData) {
						note.setLatitude(locationData.latitude);
						note.setLongitude(locationData.longitude);
						note.setAddressLine(locationData.addressLine);
					}
				}
				if(hasTweet) {
					String tweet = loadTweet(id);
					if( !tweet.equals("") ) note.setTweetContent(tweet);
				}
				
				currentNotes.add(note);
			} while(cursor.moveToNext());
			cursor.close();
		}
	}

	private String loadTweet(long id) {
		String selection = NOTE_ID + " = ?";
		String[] selectionArgs = new String[] { id+"" };
		Cursor cursor = db.query(TWEET_TABLE, new String[]{ NOTE_ID, NOTE_TWEET },
						selection, selectionArgs, null, null, null);
		cursor.moveToFirst();
		if(!cursor.isAfterLast()) {
			String tweet = cursor.getString(1);
			cursor.close();
			return tweet;
		}
		else {
			cursor.close();
			return "";
		}
	}

	private LocationData loadLocation(long id) {
		String selection = NOTE_ID + " = ?";
		String[] selectionArgs = new String[] { id+"" };
		Cursor cursor = db.query(LOCATIONS_TABLE, new String[]{ NOTE_ID, NOTE_LATITUDE, NOTE_LONGITUDE, NOTE_ADDRESS },
						selection, selectionArgs, null, null, null);
		cursor.moveToFirst();
		if(!cursor.isAfterLast()) {
			LocationData locationData = new LocationData(cursor.getString(3), cursor.getDouble(1), cursor.getDouble(2));
			cursor.close();
			return locationData;
		}
		else {
			cursor.close();
			return null;
		}
	}

	private String loadPassword(long id) {
		String selection = NOTE_ID + " = ?";
		String[] selectionArgs = new String[] { id+"" };
		Cursor cursor = db.query(PASSWORDS_TABLE, new String[]{ NOTE_ID, NOTE_PASSWORD },
						selection, selectionArgs, null, null, null);
		cursor.moveToFirst();
		if(!cursor.isAfterLast()) {
			String password = cursor.getString(1);
			cursor.close();
			return password;
		}
		else {
			cursor.close();
			return "";
		}
	}

	private void loadPrefs() {
		twitterUsername = prefs.getString(TWITTER_USERNAME_KEY, "");
		twitterPassword = prefs.getString(TWITTER_PASSWORD_KEY, "");
		useLocation = prefs.getBoolean(USE_LOCATION_KEY, false);
		useNotification = prefs.getBoolean(USE_NOTIFICATION, false);	
		notificationDistance = Integer.parseInt( prefs.getString(NOTIFICATION_DISTANCE_KEY, "200") );
		token = prefs.getString(AUTH_KEY, null);
		tokenSecret = prefs.getString(AUTH_SECRET_KEY, null);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		loadPrefs();
	}
	
	public void addNote(Note note) {
		if(note==null) return;
		
		ContentValues values = new ContentValues();
		values.put(NOTE_TITLE, note.getTitle());
		values.put(NOTE_CONTENT, note.getContent());
		values.put(NOTE_DATE, note.getCreationDateToMillis() );
		values.put(NOTE_HASPASSWORD, String.valueOf(note.isHasPassword()));
		values.put(NOTE_HASLOCATION, String.valueOf(note.isHasLocation()));
		values.put(NOTE_HASTWEET, String.valueOf(note.isHasTweet()));
		values.put(NOTE_COMPLETE, String.valueOf(note.isComplete()));
		
		note.setId( db.insert(NOTES_TABLE, null, values) );
		
		if(note.isHasTweet()) {
			addTweet(note);
		}
		if(note.isHasLocation()) {
			addLocation(note);
		}
		
		currentNotes.add(note);
	}

	private void addLocation(Note note) {
		ContentValues values = new ContentValues();
		values.put(NOTE_ID, note.getId());
		values.put(NOTE_LATITUDE, note.getLatitude());
		values.put(NOTE_LONGITUDE, note.getLongitude());
		values.put(NOTE_ADDRESS, note.getAddressLine());
		
		db.insert(LOCATIONS_TABLE, null, values);
	}

	private void addTweet(Note note) {
		ContentValues values = new ContentValues();
		values.put(NOTE_ID, note.getId());
		values.put(NOTE_TWEET, note.getTweetContent());
		
		db.insert(TWEET_TABLE, null, values);
	}
	
	public void addPassword(Note note) {
		ContentValues values = new ContentValues();
		values.put(NOTE_ID, note.getId());
		values.put(NOTE_PASSWORD, note.getPassword());
		
		db.insert(PASSWORDS_TABLE, null, values);
	}

	public String getTwitterUsername() {
		return twitterUsername;
	}

	public String getTwitterPassword() {
		return twitterPassword;
	}

	public boolean isUseLocation() {
		return useLocation;
	}

	public int getNotificationDistance() {
		return notificationDistance;
	}

	public ArrayList<Note> getNotes() {
		return currentNotes;
	}

	public void updateNote(Note item) {
		if(item==null) return;
		
		ContentValues values = new ContentValues();
		values.put(NOTE_TITLE, item.getTitle());
		values.put(NOTE_CONTENT, item.getContent());
		values.put(NOTE_DATE, item.getCreationDateToMillis());
		values.put(NOTE_HASPASSWORD, String.valueOf(item.isHasPassword()));
		values.put(NOTE_HASLOCATION, String.valueOf(item.isHasLocation()));
		values.put(NOTE_HASTWEET, String.valueOf(item.isHasTweet()));
		values.put(NOTE_COMPLETE, String.valueOf(item.isComplete()));
		
		long id = item.getId();
		String whereClause = String.format("%s = ?", NOTE_ID);
		String[] whereArgs = new String[]{ id+"" };
		db.update(NOTES_TABLE, values, whereClause, whereArgs);
		
		if( item.isHasPassword() ) {
			updatePasswords(item);
		}
	}

	private void updatePasswords(Note note) {
		ContentValues values = new ContentValues();
		values.put(NOTE_PASSWORD, note.getPassword());
		
		long id = note.getId();
		String whereClause = String.format("%s = ?", NOTE_ID);
		String[] whereArgs = new String[]{ id+"" };
		if( db.update(PASSWORDS_TABLE, values, whereClause, whereArgs) == 0 ) addPassword(note);
	}

	public void deleteNotes(Long[] ids) {
		StringBuffer idList = new StringBuffer();
		for(int i=0; i<ids.length; i++) {
			idList.append(ids[i]);
			if(i<ids.length-1) {
				idList.append(",");
			}
		}
		String whereClause = String.format("%s in (%s)", NOTE_ID, idList);
		db.delete(NOTES_TABLE, whereClause, null);
		db.delete(PASSWORDS_TABLE, whereClause, null);
		db.delete(LOCATIONS_TABLE, whereClause, null);
		db.delete(TWEET_TABLE, whereClause, null);
	}

	public boolean isUseLocationDuringSleep() {
		return useLocationDuringSleep;
	}

	public void setAdapter(NoteListAdapter adapter) {
		this.adapter = adapter;
	}

	public void setPassword(Note note) {
		adapter.setPassword(note);
		updateNote(note);
	}

	public boolean isUseNotification() {
		return useNotification;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public void tweet(String tweetContent) {
		
	}

	public boolean isAuthorized() {
		return oAuthHelper.hasAccessToken();
	}

	public String beginAuthorization() {
		try {
			System.setProperty("twitter4j.http.useSSL", "false"); 
			if( null==currentRequestToken ) {
				currentRequestToken = twitter.getOAuthRequestToken();
			}
			return currentRequestToken.getAuthorizationURL();
		}
		catch (TwitterException e) {
			e.printStackTrace();
			Log.d(DEBUG, e.getMessage()); 
			return null;
		}
	}

	public void authorized() {
		try {
			AccessToken accessToken = twitter.getOAuthAccessToken();
			storeAccessToken(accessToken);
		}
		catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	private void storeAccessToken(AccessToken accessToken) {
		oAuthHelper.setAccessToken(accessToken);
		Editor editor = prefs.edit();
		editor.putString(AUTH_KEY, accessToken.getToken());
		editor.putString(AUTH_SECRET_KEY, accessToken.getTokenSecret());
		editor.commit();
	}

	public Twitter getTwitter() {
		return twitter;
	}

	public void setTwitter(Twitter twitter) {
		this.twitter = twitter;
	}
	
}
