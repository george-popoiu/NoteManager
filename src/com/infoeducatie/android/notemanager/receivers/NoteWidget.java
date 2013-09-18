package com.infoeducatie.android.notemanager.receivers;

import java.util.ArrayList;

import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.activities.NoteListActivity;
import com.infoeducatie.android.notemanager.providers.NotesProvider;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import static com.infoeducatie.android.notemanager.notes.NoteSQLiteOpenHelper.*;

public class NoteWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		Cursor cursor = context.getContentResolver().query(NotesProvider.CONTENT_URI, 
													new String[] { NOTE_TITLE }, null, null, NOTE_ID + " DESC");
		cursor.moveToFirst();		
		if( !cursor.isAfterLast() ) {
			String noteTitle = cursor.getString(0);
			cursor.close();
			
			for( int appWidgetId : appWidgetIds ) {
				Intent intent = new Intent(context, NoteListActivity.class);
				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				
				RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
				view.setTextViewText(R.id.widget_text, noteTitle);
				view.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
				
				appWidgetManager.updateAppWidget(appWidgetId, view);
			}
		}
		else {
			cursor.close();
			return;
		}		
	}
	
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String widgetText) {
		Intent intent = new Intent(context, NoteListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		view.setTextViewText(R.id.widget_text, widgetText);
		view.setOnClickPendingIntent(R.id.widget_text, pendingIntent);
		
		appWidgetManager.updateAppWidget(appWidgetId, view);
	}

}
