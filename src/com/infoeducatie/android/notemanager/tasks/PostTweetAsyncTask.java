package com.infoeducatie.android.notemanager.tasks;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.util.Log;

public class PostTweetAsyncTask extends AsyncTask<String,Void,Void> {
	
	public interface PostTweetResponder {
		public void tweetPosting();
		public void tweetPosted();
	}
	
	Twitter twitter;
	PostTweetResponder responder;
	
	public PostTweetAsyncTask(Twitter twitter, PostTweetResponder responder) {
		super();
		this.twitter = twitter;
		this.responder = responder;
	}

	@Override
	protected Void doInBackground(String... params) {
		String tweet = params[0];
		try {
			twitter.updateStatus(tweet);
		}
		catch (TwitterException e) {
			e.printStackTrace();
			Log.d("DEBUG",e.getMessage());
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		responder.tweetPosted();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		responder.tweetPosting();
	}
	
}
