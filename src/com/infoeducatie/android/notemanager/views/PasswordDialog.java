package com.infoeducatie.android.notemanager.views;

import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.activities.NoteListActivity;
import com.infoeducatie.android.notemanager.notes.Note;
import com.infoeducatie.android.notemanager.views.PasswordDialog.PasswordDialogOnResultListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PasswordDialog extends Dialog  {

	private PasswordDialogOnResultListener listener;

	public PasswordDialog(Context context, PasswordDialogOnResultListener listener) {
		super(context);
		this.listener = listener;
	}

	private EditText passwordEdit;
	private Button okButton;
	public Note note;
	public int position;

	public interface PasswordDialogOnResultListener {
		public void onPasswordDialogResult(int position, boolean result);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.password_dialog);
		this.setCancelable(true);
		this.setTitle("Enter Password :");
		setUpViews();
	}

	private void setUpViews() {
		passwordEdit = (EditText)findViewById(R.id.password_dialog_edit);
		okButton = (Button)findViewById(R.id.password_dialog_ok);
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String enteredPassword = passwordEdit.getText().toString();
				if( enteredPassword.equals(note.getPassword()) ) {
					listener.onPasswordDialogResult(position, true);
				}
				else {
					listener.onPasswordDialogResult(-1, false);
				}
				dismiss();
			}
		});
	}

}
