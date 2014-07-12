package org.jnegre.android.osmonthego.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PadExtensionFragment extends Fragment {

	public final static String LAYOUT = "LAYOUT";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int layout = getArguments().getInt(LAYOUT);
		return inflater.inflate(layout, container, false);
	}
}
