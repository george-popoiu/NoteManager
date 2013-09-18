package com.infoeducatie.android.notemanager.providers;

import com.infoeducatie.android.notemanager.notes.NoteSQLiteOpenHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import static com.infoeducatie.android.notemanager.notes.NoteSQLiteOpenHelper.*;

public class NotesProvider extends ContentProvider {
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.infoeducatie.android.notemanager.providers.noteprovider");
	
	private NoteSQLiteOpenHelper helper;
	private SQLiteDatabase db;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		helper = new NoteSQLiteOpenHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		db = helper.getWritableDatabase();
		return db.query(NOTES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
