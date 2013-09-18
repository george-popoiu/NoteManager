package com.infoeducatie.android.notemanager.activities;

import java.util.ArrayList;

import com.infoeducatie.android.notemanager.NoteManagerApplication;
import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.notes.Note;
import com.infoeducatie.android.notemanager.receivers.NoteWidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

public class WidgetConfigurationActivity extends Activity {
	
	private int appWidgetId;
	private ArrayList<Note> notes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setVisible(false);
		//setTheme(R.style.InvisibleActivity);
		
		//in case the user backs out
		setResult(RESULT_CANCELED);
		
		//find the widget id
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if(extras!=null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		if( appWidgetId==AppWidgetManager.INVALID_APPWIDGET_ID ) {
			finish();
		}
		
		notes = ( (NoteManagerApplication)getApplication() ).getNotes();
		showDialog();
	}
	
	private void showDialog() {
		if( notes==null || notes.size()==0 ) {
			finish();
		}
		
		final ArrayList<String> items = new ArrayList<String>();
		for(Note note : notes) {
			items.add(note.getTitle());
		}
		
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle("Choose Note")
		.setItems(items.toArray(new String[]{}), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String widgetText = items.get(which);
				
				NoteWidget.updateAppWidget(WidgetConfigurationActivity.this, AppWidgetManager.getInstance(WidgetConfigurationActivity.this), 
											appWidgetId, widgetText);
				
				Intent resultIntent = new Intent();
				resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		})
		.create();
		dialog.show();
	}

}
