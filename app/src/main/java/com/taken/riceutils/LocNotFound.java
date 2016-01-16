package com.taken.riceutils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class LocNotFound extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle SavedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.locNotFound_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				
			}
		});
		return builder.create();
	}
}
