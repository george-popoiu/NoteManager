package com.infoeducatie.android.notemanager.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.android.maps.MyLocationOverlay;
import com.infoeducatie.android.notemanager.NoteManagerApplication;
import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.adapters.NoteListAdapter;
import com.infoeducatie.android.notemanager.notes.Note;
import com.infoeducatie.android.notemanager.receivers.AlarmReceiver;
import com.infoeducatie.android.notemanager.tasks.PostTweetAsyncTask;
import com.infoeducatie.android.notemanager.tasks.PostTweetAsyncTask.PostTweetResponder;
import com.infoeducatie.android.notemanager.views.PasswordDialog;
import com.infoeducatie.android.notemanager.views.PasswordDialog.PasswordDialogOnResultListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ClipDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NoteListActivity extends ListActivity implements LocationListener, PasswordDialogOnResultListener, OnDateSetListener, 
																OnTimeSetListener, PostTweetResponder {
    
	private static final int PICK_CONTANCT_REQUEST = 1001;

	private static final String DEBUG_TAG = "DEBUG";
	
	private TextView locationText;
	private NoteManagerApplication app;
	private NoteListAdapter adapter;
	private LocationManager locationManager;
	private LinearLayout locationLayout;
	private ToggleButton filterToggle;
	private Location latestLocation;
	private TextView addressText;
	private Geocoder geocoder;
	private Address latestAddress;
	private NotificationManager notificationManager;
	private Notification notification;
	private AlarmManager alarmManager;
	public Calendar calendar;
	private Calendar cal;
	private Note note;
	
	DatePickerDialog datePickerDialog;
	TimePickerDialog timePickerDialog;
	
	ProgressDialog progressDialog;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setUpViews();
        app = (NoteManagerApplication)this.getApplication();
        
        adapter = new NoteListAdapter(this, app.getNotes());
        app.setAdapter(adapter);
        this.setListAdapter(adapter);
        this.registerForContextMenu(this.getListView());
        
        setUpLocation();
        
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notification = new Notification(R.drawable.ic_menu_info_details, "", 0);
        
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        
        calendar = Calendar.getInstance();
    }

	private void setUpLocation() {
		locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
		geocoder = new Geocoder(this);
	}

	private void setUpViews() {
		addressText = (TextView)this.findViewById(R.id.address_text);
		locationText = (TextView)this.findViewById(R.id.location_text);
		locationLayout = (LinearLayout)this.findViewById(R.id.location_layout);
		filterToggle = (ToggleButton)this.findViewById(R.id.filter_button);
		
		filterToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				filterNotessByLocation(isChecked);
			}
		});
	}
	
	protected void filterNotessByLocation(boolean isChecked) {
		if(isChecked) {
			adapter.filterNotesByLocation(latestLocation, app.getNotificationDistance());
		}
		else {
			adapter.removeLocationFilter();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.forceReload();
		if(app.isUseLocation()) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, app.getNotificationDistance(), this);
		}
		else {
			locationLayout.setVisibility(View.GONE);
			locationManager.removeUpdates(this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		adapter.setAllEnteredPassword(false);
	}
	
	protected void addNote() {
		Intent intent = new Intent(this, AddNoteActivity.class);
		this.startActivity(intent);
	}
	
	public void removeComplete() {
		if( app.isAuthorized() ) {
			ArrayList<Note> notes = adapter.getNotes();
			for( Note note : notes ) {
				if(note.isComplete()==true && ( !note.isHasPassword() || note.isEnteredPassword()==true ) && note.isHasTweet() ) {
					new PostTweetAsyncTask(app.getTwitter(), this).execute(note.getTweetContent());
				}
			}
		}
		
		Long[] ids = adapter.removeCompleted();
		app.deleteNotes(ids);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Note note = adapter.getItem(position);
		if( note.isHasPassword()==true && note.isEnteredPassword() == false) {
			passwordCheck(note, position);
			return;
		}
		adapter.toggleComplete(position);
		app.updateNote(adapter.getItem(position));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return new MenuHelper().StartActivityFromMenuItem(this, item, this);
	}
	
	public void viewNote(long id) {
		Note note = adapter.getItem((int)id);
		if( note.isHasPassword()==true && note.isEnteredPassword() == false) {
			passwordCheck(note, (int)id);
			return;
		}
		Intent intent = new Intent(this, ViewNoteActivity.class);
		intent.putExtra("NOTE", note);
		startActivity(intent);
	}
	
	public void removeNote(long id) {
		Note note = adapter.getItem((int)id);
		if( note.isHasPassword()==true && note.isEnteredPassword() == false) {
			passwordCheck(note, (int)id);
			return;
		}
		if( note.isHasTweet() ) {
			new PostTweetAsyncTask(app.getTwitter(), this).execute(note.getTweetContent());
		}
		adapter.removeAt(id);
		app.deleteNotes(new Long[]{note.getId()});
	}
	
	public void setReminder(long id) {
		note = adapter.getItem((int)id);
		if( note.isHasPassword()==true && note.isEnteredPassword() == false) {
			passwordCheck(note, (int)id);
			return;
		}
		cal = Calendar.getInstance();
		//Pick date
		datePickerDialog = new DatePickerDialog(this, this, cal.get(Calendar.YEAR), 
															cal.get(Calendar.MONTH), 
															cal.get(Calendar.DAY_OF_MONTH));
		datePickerDialog.show();
	}

	public void setPassword(long id) {
		Note note = adapter.getItem((int)id);
		if( note.isHasPassword()==true && note.isEnteredPassword() == false) {
			passwordCheck(note, (int)id);
			return;
		}
		Intent intent = new Intent(this, SetPasswordActivity.class);
		intent.putExtra("POSITION", id);
		intent.putExtra("NOTE", note);
		this.startActivity(intent);
	}
	
	public void shareNote(long id) {
		final Note note = adapter.getItem((int)id);
		if( note.isHasPassword()==true && note.isEnteredPassword() == false) {
			passwordCheck(note, (int)id);
			return;
		}
		final String[] items = new String[] { "SMS", "Mail" };
		AlertDialog shareTypeDialog = new AlertDialog.Builder(this)
		.setTitle("Share via")
		.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if( items[which].equals("SMS") ) {
					sendSMS(note);
				}
				else {
					sendMail(note);
				}
				dialog.dismiss();
			}
		})
		.create();
		shareTypeDialog.show();
	}
	
	protected void sendMail(Note note) {
		Intent mailIntent = new Intent(Intent.ACTION_SEND);
		mailIntent.setType("plain/text");
		mailIntent.putExtra(Intent.EXTRA_TEXT, buildMessage(note));
		startActivity( Intent.createChooser(mailIntent, "Send your email using :") );
	}

	String buildMessage(Note note) {
		StringBuffer messageBody = new StringBuffer();
		messageBody.append("Title : " + note.getTitle() + "\n");
		messageBody.append(note.getContent()+"\n");
		if( note.isHasLocation() ) {
			messageBody.append("Address : " + note.getAddressLine() + "\n");
		}
		if( note.isHasTweet() ) {
			messageBody.append("Tweet : " + note.getTweetContent());
		}
		return messageBody.toString();
	}
	
	protected void sendSMS(Note note) {
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body", buildMessage(note));
		sendIntent.setType("vnd.android-dir/mms-sms");
		startActivity(sendIntent);
	}

	void passwordCheck(Note note, int position) {
		PasswordDialog dialog = new PasswordDialog(this, this);
		dialog.note = note;
		dialog.position = position;
		dialog.show();
	}
	
	public class MenuHelper {
		
		public boolean StartActivityFromMenuItem(Context context, MenuItem item, final NoteListActivity caller) {
			
			AdapterContextMenuInfo info;
			Intent intent;
			Class<? extends Activity> requestingClass = (Class<? extends Activity>) context.getClass();

			switch(item.getItemId()) {
				case R.id.prefs_menu_item :
					if( requestingClass != PreferencesActivity.class ) {
						intent = new Intent(context, PreferencesActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
						return true;
					}
					break;
				case R.id.add_menu_item:
					context.startActivity(new Intent(context, AddNoteActivity.class));
					return true;
				case R.id.remove_menu_item :
					caller.removeComplete();
					return true;
				case R.id.context_menu_view:
					info = (AdapterContextMenuInfo)item.getMenuInfo();
					caller.viewNote(info.id);
					return true;
				case R.id.context_menu_remove:
					info = (AdapterContextMenuInfo)item.getMenuInfo();
					caller.removeNote(info.id);
					return true;
				case R.id.context_menu_password:
					info = (AdapterContextMenuInfo)item.getMenuInfo();
					caller.setPassword(info.id);
					return true;
				case R.id.context_menu_share:
					info = (AdapterContextMenuInfo)item.getMenuInfo();
					caller.shareNote(info.id);
					return true;
				case R.id.context_menu_reminder:
					info = (AdapterContextMenuInfo)item.getMenuInfo();
					caller.setReminder(info.id);
					return true;
			}
			return false;
		}
		
	}

	@Override
	public void onLocationChanged(Location location) {
		locationLayout.setVisibility(View.VISIBLE);
		latestLocation = location;
		String locationString = String.format("@ %f, %f", location.getLatitude(), location.getLongitude());
		locationText.setText(locationString);
		
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if(null==addresses || addresses.size()==0) return;
			latestAddress = addresses.get(0);
			StringBuffer addressString = new StringBuffer();
			if(latestAddress.getAddressLine(0)!=null) addressString.append(latestAddress.getAddressLine(0)+"\n");
			if(latestAddress.getAddressLine(1)!=null) addressString.append(latestAddress.getAddressLine(1)+"\n");
			if(latestAddress.getAddressLine(2)!=null) addressString.append(latestAddress.getAddressLine(2)+"\n");
			addressString.deleteCharAt(addressString.length()-1);
			addressText.setText(addressString);
			
			if( app.isUseNotification() ) {
				ArrayList<Note> notes = adapter.getNotes();
				for( Note note : notes ) {
					if( note.isHasLocation() && adapter.noteIsWithinDistance(note, latestLocation, app.getNotificationDistance())==true ) {
						notifyNoteIsNear(note, app.getNotificationDistance());
					}
				}
			}
		}
		catch (IOException e) {
			Log.d("DEBUG",e.getMessage());
		}
	}

	private void notifyNoteIsNear(Note note, int notificationDistance) {
		PendingIntent pendingIntent = PendingIntent.getActivity(this, -1, 
																new Intent(this, NoteListActivity.class), 
																PendingIntent.FLAG_UPDATE_CURRENT);	
		String notificationTitle = "Note Manager - Note nearby";
		String notificationSummary = note.getTitle();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this, notificationTitle, notificationSummary, pendingIntent);
		notificationManager.notify(0, notification);
	}

	@Override
	public void onProviderDisabled(String provider) {
		locationLayout.setVisibility(View.GONE);
	}

	@Override
	public void onProviderEnabled(String provider) {
		locationLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) { }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return new MenuHelper().StartActivityFromMenuItem(this, item, this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public void onPasswordDialogResult(int position, boolean result) {
		if(result==true) {
			adapter.setEnteredPassword(position, true);
			Toast.makeText(this, "Note Unlocked", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, monthOfYear);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		datePickerDialog.dismiss();
		
		//pick time
		cal = Calendar.getInstance();
		timePickerDialog = new TimePickerDialog(this, this, cal.get(Calendar.HOUR_OF_DAY), 
												cal.get(Calendar.MINUTE), true);
		timePickerDialog.show();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		timePickerDialog.dismiss();
		
		Intent broadcastIntent = new Intent(this, AlarmReceiver.class);
		broadcastIntent.putExtra("NOTE", note);
		PendingIntent operation = PendingIntent.getBroadcast(this, 0, 
															broadcastIntent, 
															PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA );
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
	}

	@Override
	public void tweetPosting() {
		if( progressDialog==null ) {
			progressDialog = ProgressDialog.show(this, "Posting Tweet", "Please wait...");
		}
	}

	@Override
	public void tweetPosted() {
		if( progressDialog!=null ) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

}