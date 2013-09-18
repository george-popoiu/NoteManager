package com.infoeducatie.android.notemanager.receivers;

import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.activities.NoteListActivity;
import com.infoeducatie.android.notemanager.notes.Note;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	private NotificationManager notificationManager;
	private Notification notification;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Note note = intent.getParcelableExtra("NOTE");
		
		notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.ic_menu_info_details,  "", System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		String notificationTitle = "Note Manager - Reminder";
		String notificationSummary = note.getTitle();
		
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, 
																new Intent(context, NoteListActivity.class), 
																PendingIntent.FLAG_UPDATE_CURRENT); 
		
		notification.setLatestEventInfo(context, notificationTitle, notificationSummary, pendingIntent);
		notificationManager.notify(1, notification);
		
		Log.d("DEBUG", note.getTitle());
	}

}
