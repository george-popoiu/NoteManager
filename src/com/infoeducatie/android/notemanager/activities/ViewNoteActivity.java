package com.infoeducatie.android.notemanager.activities;

import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.notes.Note;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ViewNoteActivity extends Activity {

	private TextView title;
	private TextView content;
	private TextView location;
	private TextView tweet;
	private TextView beforeTweet;
	private TextView beforeLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.view_note);
		setUpViews();
		displayNoteData();
	}

	private void displayNoteData() {
		Note note = getIntent().getParcelableExtra("NOTE");
		title.setText(note.getTitle());
		if(!note.getContent().equals("")) {
			content.setVisibility(View.VISIBLE);
			content.setText(note.getContent());
		}
		if(!note.getAddressLine().equals("")) {
			beforeLocation.setVisibility(View.VISIBLE);
			location.setVisibility(View.VISIBLE);
			location.setText(note.getAddressLine());
		}
		if(!note.getTweetContent().equals("")) {
			beforeTweet.setVisibility(View.VISIBLE);
			tweet.setVisibility(View.VISIBLE);
			tweet.setText(note.getTweetContent());
		}
	}

	private void setUpViews() {
		title = (TextView)this.findViewById(R.id.view_note_title);
		content = (TextView)this.findViewById(R.id.view_note_content);
		beforeLocation = (TextView)this.findViewById(R.id.view_note_before_location);
		location = (TextView)this.findViewById(R.id.view_note_location);
		beforeTweet = (TextView)this.findViewById(R.id.view_note_before_tweet);
		tweet = (TextView)this.findViewById(R.id.view_note_tweet);
	}
	
}
