package com.infoeducatie.android.notemanager.notes;

import android.location.Geocoder;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TwoLineListItem;


public class Note implements Parcelable {
	
	long id;//13
	String title = "";//1
	String content = "";//2
	String password = "";//8
	long creationDateToMillis;//7
	double latitude = 0;//5
	double longitude = 0;//6
	
	boolean complete = false;//9
	boolean hasPassword = false;//10
	boolean hasLocation = false;//11
	boolean hasTweet = false;//12
	private String tweetContent = "";//4
	private String addressLine = "";//3
	private boolean enteredPassword = false;
	
	@Override
	public int describeContents() {
		return this.hashCode();
	}

	public static final Creator<Note> CREATOR = new Creator<Note>() {
		@Override
		public Note[] newArray(int size) {
			return new Note[size];
		}
		@Override
		public Note createFromParcel(Parcel source) {
			return new Note(source);
		}
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(content);
		dest.writeString(addressLine);
		dest.writeString(tweetContent);
		dest.writeDouble(latitude); dest.writeDouble(longitude);
		dest.writeLong(creationDateToMillis);
		dest.writeString(password);
		dest.writeString(String.valueOf(complete)); dest.writeString(String.valueOf(hasPassword));
		dest.writeString(String.valueOf(hasLocation)); dest.writeString(String.valueOf(hasTweet));
		dest.writeLong(id);
	}
	
	public Note(Parcel source) {
		title = source.readString();
		content = source.readString();
		addressLine = source.readString();
		tweetContent = source.readString();
		latitude = source.readDouble(); longitude = source.readDouble();
		creationDateToMillis = source.readLong();
		password = source.readString();
		complete = Boolean.parseBoolean(source.readString()); hasPassword = Boolean.parseBoolean(source.readString());
		hasLocation = Boolean.parseBoolean(source.readString()); hasTweet = Boolean.parseBoolean(source.readString());
		id = source.readLong();
	}
	
	public Note() { }
	
	public Note(String title, String content, long creationDateToMillis, double latitude, double longitude, String addressLine, 
				boolean hasTweet, String tweetContent) { 
		super();
		this.title = title;
		this.content = content;
		this.latitude = latitude;
		this.longitude = longitude;
		this.hasTweet = hasTweet;
		this.tweetContent = tweetContent ;
		this.creationDateToMillis = creationDateToMillis;
		this.addressLine = addressLine;
		
		this.hasLocation = true;
	}
	
	public Note(String title, String content, long creationDateToMillis, boolean hasTweet, String tweetContent) { //ToDo
		super();
		this.title = title;
		this.content = content;
		this.hasTweet = hasTweet;
		this.tweetContent = tweetContent;
		this.creationDateToMillis = creationDateToMillis;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAddressLine() {
		return addressLine;
	}

	public void setAddressLine(String addressLine) {
		this.addressLine = addressLine;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public boolean isHasPassword() {
		return hasPassword;
	}
	public void setHasPassword(boolean hasPassword) {
		this.hasPassword = hasPassword;
	}
	public boolean isHasLocation() {
		return hasLocation;
	}
	public void setHasLocation(boolean hasLocation) {
		this.hasLocation = hasLocation;
	}
	public boolean isHasTweet() {
		return hasTweet;
	}
	public void setHasTweet(boolean hasTweet) {
		this.hasTweet = hasTweet;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public long getCreationDateToMillis() {
		return creationDateToMillis;
	}

	public void setCreationDateToMillis(long creationDateToMillis) {
		this.creationDateToMillis = creationDateToMillis;
	}

	public String getTweetContent() {
		return tweetContent;
	}

	public void setTweetContent(String tweetContent) {
		this.tweetContent = tweetContent;
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	public boolean isComplete() {
		return this.complete;
	}

	public void toggleComplete() {
		complete = !complete;
	}

	public void setEnteredPassword(boolean enteredPassword) {
		this.enteredPassword = enteredPassword;
	}

	public boolean isEnteredPassword() {
		return enteredPassword;
	}

}
