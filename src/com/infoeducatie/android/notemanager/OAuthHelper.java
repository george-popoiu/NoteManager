package com.infoeducatie.android.notemanager;

import static com.infoeducatie.android.notemanager.NoteManagerApplication.*;

import java.io.InputStream;
import java.util.Properties;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

import android.content.Context;
import android.util.Log;

public class OAuthHelper {
	
	private Context context;
	private NoteManagerApplication app;
	private String consumerKey;
	private String consumerSecretKey;
	private AccessToken accessToken;

	public OAuthHelper(Context context, NoteManagerApplication app) {
		super();
		this.context = context;
		this.app = app;
		loadConsumerKeys();
		accessToken = loadAccessToken();
	}
	
	private AccessToken loadAccessToken() {
		String token = app.getToken();
		String tokenSecret = app.getTokenSecret();
		if( null!=token && null!=tokenSecret ) {
			return new AccessToken(token, tokenSecret);
		}
		return null;
	}

	private void loadConsumerKeys() {
		try {
			Properties props = new Properties();
			InputStream stream = context.getResources().openRawResource(R.raw.oauth);
			props.load(stream);
			consumerKey = (String)props.get("consumer_key");
			consumerSecretKey = (String)props.get("consumer_secret_key");
		}
		catch (Exception e) {
			Log.d(DEBUG, e.getMessage());
		}
	}

	public void configureOAuth(Twitter twitter) {
		twitter.setOAuthConsumer(consumerKey, consumerSecretKey);
		twitter.setOAuthAccessToken(accessToken);
	}

	public boolean hasAccessToken() {
		return ( accessToken!=null );
	}

	public AccessToken getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}
	
}
