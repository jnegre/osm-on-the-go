package org.jnegre.android.osmonthego.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jnegre.android.osmonthego.R;
import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData;

public class AddressActivity extends Activity {

	private final static String TAG = "AddressActivity";

	public final static String EXTRA_LATITUDE = "lat";
	public final static String EXTRA_LONGITUDE = "long";

	private static enum PadExtension {
		LOWER(R.id.action_address_lowercase, R.layout.frag_lowercase),
		UPPER(R.id.action_address_uppercase, R.layout.frag_uppercase),
		LATIN(R.id.action_address_latin, R.layout.frag_latin);

		private final int action;
		private final int layout;

		PadExtension(int action, int layout) {
			this.action = action;
			this.layout = layout;
		}

		int getLayout() {
			return layout;
		}

		public int getAction() {
			return action;
		}
	}

	private void showPadExtension(PadExtension pe, MenuItem menuItem) {
		if (menuItem != null) {
			menuItem.setChecked(true);
		}

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.extension_pad, new PadExtensionFragment(pe.getLayout()));
		fragmentTransaction.commit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_address);
		PadExtension savedPE = PadExtension.LATIN; //TODO save this
		showPadExtension(savedPE, null);
	}

	public void onKeyPadButton(View target) {
		EditText edit = (EditText) this.findViewById(R.id.addr_number);
		CharSequence input = ((Button) target).getText();
		edit.getText().append(input);
	}

	public void onClearButton(View target) {
		EditText edit = (EditText) this.findViewById(R.id.addr_number);
		edit.getText().clear();
	}

	public void onOK(View target) {
		Log.d(TAG, "Inserting new address");
		Intent startIntent = getIntent();
		String number = ((EditText) this.findViewById(R.id.addr_number)).getText().toString();
		String street = ((EditText) this.findViewById(R.id.addr_street)).getText().toString();
		if (number.length() != 0) {
			ContentValues cv = new ContentValues();
			cv.put(SurveyProviderMetaData.AddressTableMetaData.LATITUDE, startIntent.getDoubleExtra(EXTRA_LATITUDE, 0));
			cv.put(SurveyProviderMetaData.AddressTableMetaData.LONGITUDE, startIntent.getDoubleExtra(EXTRA_LONGITUDE, 0));
			cv.put(SurveyProviderMetaData.AddressTableMetaData.NUMBER, number);
			if (street.length() != 0) {
				cv.put(SurveyProviderMetaData.AddressTableMetaData.STREET, street);
			}
			ContentResolver cr = getContentResolver();
			Uri uri = SurveyProviderMetaData.AddressTableMetaData.CONTENT_URI;
			Log.d(TAG, "address insert uri:" + uri);
			Uri insertedUri = cr.insert(uri, cv);
			Log.d(TAG, "inserted uri:" + insertedUri);
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.address, menu);
		PadExtension savedPE = PadExtension.LATIN; //TODO save this
		MenuItem menuItem = menu.findItem(savedPE.getAction());
		menuItem.setChecked(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_address_lowercase:
				showPadExtension(PadExtension.LOWER, item);
				return true;
			case R.id.action_address_uppercase:
				showPadExtension(PadExtension.UPPER, item);
				return true;
			case R.id.action_address_latin:
				showPadExtension(PadExtension.LATIN, item);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}