package org.jnegre.android.osmonthego;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData;

public abstract class AddressIterator {

	private final Context context;

	public AddressIterator(Context context) {
		this.context = context;
	}

	public void iterate() {
		double lat, lng;
		String number, street;

		Uri uri = SurveyProviderMetaData.AddressTableMetaData.CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri,
				null, //projection
				null, //selection string
				null, //selection args array of strings
				null); //sort order
		try {
			int iLat = cursor.getColumnIndex(SurveyProviderMetaData.AddressTableMetaData.LATITUDE);
			int iLong = cursor.getColumnIndex(SurveyProviderMetaData.AddressTableMetaData.LONGITUDE);
			int iNumber = cursor.getColumnIndex(SurveyProviderMetaData.AddressTableMetaData.NUMBER);
			int iStreet = cursor.getColumnIndex(SurveyProviderMetaData.AddressTableMetaData.STREET);

			onBeforeAddresses();

			//walk through the rows based on indexes
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				//Gather values
				lat = cursor.getDouble(iLat);
				lng = cursor.getDouble(iLong);
				number = cursor.getString(iNumber);
				street = cursor.getString(iStreet);
				onAddress(lat, lng, number, street);
			}
		} finally {
			cursor.close();
		}
		onAfterAddresses();
	}

	protected void onBeforeAddresses() {
	}

	protected void onAfterAddresses() {
	}


	protected abstract void onAddress(double lat, double lng, String number, String street);
}
