/*
 * Copyright 2014  Jérôme Nègre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jnegre.android.osmonthego.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.jnegre.android.osmonthego.R;
import org.jnegre.android.osmonthego.service.SurveyService;

public class FixmeDialogFragment extends DialogFragment {
	private final static String ARG_LATITUDE = "LATITUDE";
	private final static String ARG_LONGITUDE = "LONGITUDE";

	private EditText commentEditText;

	public static FixmeDialogFragment newInstance(double lat, double lng) {
		FixmeDialogFragment dialog = new FixmeDialogFragment();

		Bundle args = new Bundle();
		args.putDouble(ARG_LATITUDE, lat);
		args.putDouble(ARG_LONGITUDE, lng);
		dialog.setArguments(args);

		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.activity_fixme, null);
		this.commentEditText = ((EditText) view.findViewById(R.id.fixme));

		this.commentEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					insertFixme();
					return true;
				}
				return false;
			}
		});

		builder
			.setTitle(R.string.label_fixme)
			.setView(view)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					insertFixme();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {

				}
			});

		return builder.create();
	}

	private void insertFixme() {
		String comment = commentEditText.getText().toString();

		if(comment!=null && !comment.isEmpty()) {
			Bundle args = getArguments();
			double lat = args.getDouble(ARG_LATITUDE);
			double lng = args.getDouble(ARG_LONGITUDE);
			SurveyService.startInsertFixme(getActivity(), lat, lng, comment);
		}
		this.dismiss();
	}
}
