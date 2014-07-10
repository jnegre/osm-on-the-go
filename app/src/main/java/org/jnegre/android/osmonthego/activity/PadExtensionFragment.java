package org.jnegre.android.osmonthego.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PadExtensionFragment extends Fragment {

	private final int layout;

	public PadExtensionFragment(int layout) {
		this.layout = layout;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layout, container, false);
	}
}
