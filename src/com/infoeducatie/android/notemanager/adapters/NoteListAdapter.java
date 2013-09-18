package com.infoeducatie.android.notemanager.adapters;

import java.util.ArrayList;

import com.infoeducatie.android.notemanager.NoteManagerApplication;
import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.notes.Note;
import com.infoeducatie.android.notemanager.views.NoteListItem;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class NoteListAdapter extends BaseAdapter {
	
	private ArrayList<Note> notes, filteredNotes, unfilteredNotes;
	private Context context;
	boolean isFiltered;

	public NoteListAdapter(Context context, ArrayList<Note> notes) {
		super();
		this.notes = notes;
		this.unfilteredNotes = notes;
		this.context = context;
	}

	@Override
	public int getCount() {
		return notes.size();
	}

	@Override
	public Note getItem(int position) {
		return ( (null==notes) ? null : notes.get(position) );
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NoteListItem nli = null;
		if(null==convertView) {
			nli = (NoteListItem)View.inflate(context, R.layout.note_list_item, null);
		}
		else {
			nli = (NoteListItem)convertView;
		}
		nli.setNote( notes.get(position) );
		return nli;
	}

	public void forceReload() {
		this.notifyDataSetChanged();
	}

	public void toggleComplete(int position) {
		Note note = notes.get(position);
		note.toggleComplete();
		this.notifyDataSetChanged();
	}

	public Long[] removeCompleted() {
		ArrayList<Note> completedNotes = new ArrayList<Note>();
		ArrayList<Long> ids = new ArrayList<Long>();
		
		for( Note note : notes ) {
			if(note.isComplete()==true && ( !note.isHasPassword() || note.isEnteredPassword()==true ) ) {
				completedNotes.add(note);
				ids.add(note.getId());
			}
		}
		if(isFiltered==true) unfilteredNotes.removeAll(completedNotes);
		notes.removeAll(completedNotes);
		this.notifyDataSetChanged();
		return ids.toArray(new Long[]{});
	}

	public void filterNotesByLocation(Location latestLocation, int notificationDistance) {
		filteredNotes = new ArrayList<Note>();
		for( Note note : notes ) {
			if( note.isHasLocation() && noteIsWithinDistance(note,latestLocation,notificationDistance) ) {
				filteredNotes.add(note);
			}
		}
		notes = filteredNotes;
		isFiltered = true;
		notifyDataSetChanged();
	}

	public boolean noteIsWithinDistance(Note note, Location latestLocation, int notificationDistance) {
		float[] result = new float[1];
		Location.distanceBetween(note.getLatitude(), note.getLongitude(), 
								latestLocation.getLatitude(), latestLocation.getLongitude(), result);
		return ( result[0] <= notificationDistance );
	}

	public void removeLocationFilter() {
		notes = unfilteredNotes;
		isFiltered = false;
		this.notifyDataSetChanged();
	}

	public void removeAt(long id) {
		if(isFiltered) {
			Note note = notes.get((int)id);
			notes.remove((int)id);
			for(int i=0; i<unfilteredNotes.size(); i++) {
				if( note.getId() == unfilteredNotes.get(i).getId() ) {
					unfilteredNotes.remove(i);
					break;
				}
			}
		}
		else {
			notes.remove((int)id);
		}
		this.notifyDataSetChanged();
	}

	public void setPassword(Note note) {
		for( Note n : notes ) {
			if( n.getId() == note.getId() ) {
				n.setPassword(note.getPassword());
				n.setHasPassword(true);
				break;
			}
		}
		this.notifyDataSetChanged();
	}

	public void setEnteredPassword(int position, boolean b) {
		notes.get(position).setEnteredPassword(b);
	}

	public void setAllEnteredPassword(boolean b) {
		for(Note n : notes) {
			n.setEnteredPassword(b);
		}
	}

	public ArrayList<Note> getNotes() {
		return notes;
	}
	
}
