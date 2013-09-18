package com.infoeducatie.android.notemanager.activities;

import com.infoeducatie.android.notemanager.NoteManagerApplication;
import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.notes.Note;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddNoteActivity extends Activity {

	private static final int ADDRESS_REQUEST = 0;
	private NoteManagerApplication app;
	private EditText noteTitleEdit;
	private EditText noteContentEdit;
	private CheckBox tweetCheck;
	private TextView counterText;
	private EditText tweetEdit;
	private Address address;
	private TextView locationText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_note);
		app = (NoteManagerApplication)this.getApplication();
		setUpViews();
	}
	
	private void setUpViews() {
		noteTitleEdit = (EditText) this.findViewById(R.id.todo_note_title_edit);
		noteContentEdit = (EditText) this.findViewById(R.id.todo_note_content_edit);
		locationText = (TextView) this.findViewById(R.id.todo_note_location_text);
		
		//
		counterText = (TextView) this.findViewById(R.id.counter_text);
		tweetCheck = (CheckBox) this.findViewById(R.id.tweet_when_complete);
		tweetEdit = (EditText) this.findViewById(R.id.todo_note_tweet_content);
		
		tweetCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkedChanged(isChecked);
			}
		});
		
		tweetEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int charsLeft = 140 - s.length();
				counterText.setText(charsLeft+"");
				if(charsLeft<0) counterText.setTextColor(Color.RED);
				else counterText.setTextColor(Color.GREEN);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().length()==0) counterText.setText("");
			}
		}); 
		//
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(null!=address) {
			locationText.setVisibility(View.VISIBLE);
			locationText.setText(address.getAddressLine(0));
		}
	}

	protected void checkedChanged(boolean isChecked) {
		if( !app.isAuthorized() ) {
			tweetCheck.setChecked(false);
			
			final Dialog dialog = new Dialog(this);
			dialog.setTitle("Authorize with twitter");
			dialog.setContentView(R.layout.twitter_dialog);
			dialog.setCancelable(true);
			
			TextView dialogSummary = (TextView)dialog.findViewById(R.id.twitter_dialog_text);
			dialogSummary.setText("Authorize to allow Note Manager to post tweets.");
			
			Button setUpAccount = (Button)dialog.findViewById(R.id.twitter_dialog_button);
			setUpAccount.setText("Authorize");
			setUpAccount.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					Intent intent = new Intent(AddNoteActivity.this, AuthorizationActivity.class);
					startActivity(intent);
				}
			});
			
			dialog.show();
		}
		else {
			if(isChecked) {
				tweetEdit.setEnabled(true);
				counterText.setVisibility(View.VISIBLE);
			}
			else {
				tweetEdit.setEnabled(false);
				counterText.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	void showAlert(String alertTitle, String alertMessage) {
		AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle(alertTitle)
		.setMessage(alertMessage)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		alertDialog.show();
	}

	public void addButtonClicked(View v) {
		String noteTitle = noteTitleEdit.getText().toString();
		if(noteTitle.equals("")) {
			showAlert("Please enter a title", "Your note has to have a title");
			return;
		}
		String noteContent = noteContentEdit.getText().toString();
		
		boolean isTweet = tweetCheck.isChecked();
		String tweetContent = "";
		if( isTweet ) tweetContent = tweetEdit.getText().toString();
		if(tweetContent.length()==0) isTweet = false;
		if(isTweet==true && tweetContent.length()>140 ) {
			showAlert("Tweet too large", "Maximum 140 characters allowed");
			return;
		}
		
		Note note = null;
		if(address==null) {
			note = new Note(noteTitle, noteContent,System.currentTimeMillis(), isTweet, tweetContent);
		}
		else {
			note = new Note(noteTitle, noteContent, System.currentTimeMillis(), address.getLatitude(), address.getLongitude(), 
							address.getAddressLine(0), isTweet, tweetContent);
		}
		app.addNote(note);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.add_note_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.add_note_menu_add_location) {
			Intent intent = new Intent(this, AddLocationMapActivity.class);
			this.startActivityForResult(intent, ADDRESS_REQUEST);
			return true;
		}
		else if(item.getItemId()==R.id.add_note_menu_remove_location) {
			removeLocation();
		}
		return false;
	}

	private void removeLocation() {
		address = null;
		locationText.setText("");
		locationText.setVisibility(View.GONE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( requestCode==ADDRESS_REQUEST && resultCode==RESULT_OK ) {
			address = (Address)data.getParcelableExtra(AddLocationMapActivity.ADDRESS_RESULT);
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
}
