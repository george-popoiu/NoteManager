package com.infoeducatie.android.notemanager.views;

import java.io.IOException;
import java.util.List;

import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.notes.Note;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NoteListItem extends RelativeLayout {
	
	public static final String NoteListItem_TAG = "NoteListItem";
	
	Note note;
	private CheckedTextView checkbox;
	private ImageView passwordImage;
	private TextView locationText;
	private Context context;

	public NoteListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setTag(NoteListItem_TAG);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		checkbox = (CheckedTextView)this.findViewById(android.R.id.text1);
		passwordImage = (ImageView)this.findViewById(R.id.note_item_password_icon);
		locationText = (TextView)this.findViewById(R.id.note_item_location_text);
	}		
	
	public void setNote(Note note) {
		this.note = note;
		checkbox.setText(note.getTitle());
		checkbox.setChecked(note.isComplete());
		
		if(note.isHasPassword()) { 
			passwordImage.setVisibility(View.VISIBLE);
		}
		else {
			passwordImage.setVisibility(View.GONE);
		}
		
		if(note.isHasLocation()) {
			locationText.setVisibility(View.VISIBLE);
			locationText.setText(note.getAddressLine());
		}
		else {
			locationText.setVisibility(View.GONE);
			locationText.setText("");
		}
	}

	public Note getNote() {
		return note;
	}
	
}
