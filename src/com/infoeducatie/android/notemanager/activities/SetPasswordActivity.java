package com.infoeducatie.android.notemanager.activities;

import com.infoeducatie.android.notemanager.NoteManagerApplication;
import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.notes.Note;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetPasswordActivity extends Activity {
	
	Note note;
	private NoteManagerApplication app;
	private EditText newPasswordEdit;
	private EditText repeatPasswordEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.set_password);
		app = (NoteManagerApplication)this.getApplication();
		note = this.getIntent().getParcelableExtra("NOTE");
		setUpViews();
	}

	private void setUpViews() {
		newPasswordEdit = (EditText)this.findViewById(R.id.new_password_edit);
		repeatPasswordEdit = (EditText)this.findViewById(R.id.repeat_password_edit);
	}
	
	public void setPasswordClicked(View v) {
		String password = newPasswordEdit.getText().toString();
		String passwordRepeat = repeatPasswordEdit.getText().toString();
		if( !password.equals(passwordRepeat) ) {
			showAlert("Passwords don't match!", "Try re-entering the passwords in both fields");
		}
		else {
			note.setHasPassword(true);
			note.setPassword(password);
			app.setPassword(note);
			finish();
		}
	}

	private void showAlert(String title, String message) {
		AlertDialog alert = new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		alert.show();
	}
	
}
