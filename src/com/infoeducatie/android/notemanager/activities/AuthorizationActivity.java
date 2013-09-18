package com.infoeducatie.android.notemanager.activities;

import com.infoeducatie.android.notemanager.NoteManagerApplication;
import com.infoeducatie.android.notemanager.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthorizationActivity extends Activity {

	private NoteManagerApplication app;
	private WebView webView;
	private Handler handler = new Handler();
	private ProgressDialog progress;
	
	private WebViewClient webViewClient = new WebViewClient() {
		@Override
		public void onLoadResource(WebView view, String url) {
			Uri uri = Uri.parse(url);
			if( uri.getHost().equals("notemanager.com") ) { 
				String token = uri.getQueryParameter("oauth_token");
				if( token!=null ) {
					webView.setVisibility(View.INVISIBLE);
					Thread authorizeThread = new Thread() {
						public void run() {
							app.authorized();
							handler.post(finishAuth);
						}
					};
					progress = ProgressDialog.show(AuthorizationActivity.this, "Authorizing", "Please wait...");
					//progress.show();
					authorizeThread.start();
				}
			}
			else { 
				super.onLoadResource(view, url);
			}
		}
	};
	
	Runnable finishAuth = new Runnable() {
		@Override
		public void run() {
			progress.dismiss();
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authorization_view);
		setUpViews();
		app = (NoteManagerApplication)getApplication();
	}

	private void setUpViews() {
		webView = (WebView)findViewById(R.id.web_view);
		webView.setWebViewClient(webViewClient);
	}

	@Override
	protected void onResume() {
		super.onResume();
		String authUrl = app.beginAuthorization();
		webView.loadUrl(authUrl);
	}

}
