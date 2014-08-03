package org.jnegre.android.osmonthego.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.FixmeTableMetaData;
import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.AddressTableMetaData;

public class SurveyService extends IntentService {
	private static final String ACTION_INSERT = "org.jnegre.android.osmonthego.service.action.INSERT";
	private static final String ACTION_DELETE = "org.jnegre.android.osmonthego.service.action.DELETE";

	private static final String EXTRA_CONTENT_VALUE = "org.jnegre.android.osmonthego.service.extra.CONTENT_VALUE";

	public static void startDelete(Context context, Uri data) {
		Intent intent = new Intent(context, SurveyService.class);
		intent.setAction(ACTION_DELETE);
		intent.setData(data);
		context.startService(intent);
	}

	public static void startInsertAddress(Context context, double lat, double lng, String number, String street) {
		Intent intent = new Intent(context, SurveyService.class);
		intent.setAction(ACTION_INSERT);

		ContentValues cv = new ContentValues();
		cv.put(AddressTableMetaData.LATITUDE, lat);
		cv.put(AddressTableMetaData.LONGITUDE, lng);
		cv.put(AddressTableMetaData.NUMBER, number);
		if (street != null && street.length() != 0) {
			cv.put(AddressTableMetaData.STREET, street);
		}

		intent.setData(AddressTableMetaData.CONTENT_URI);
		intent.putExtra(EXTRA_CONTENT_VALUE, cv);
		context.startService(intent);
	}

	public static void startDeleteAddress(Context context) {
		startDelete(context, AddressTableMetaData.CONTENT_URI);
	}

	public static void startInsertFixme(Context context, double lat, double lng, String comment) {
		Intent intent = new Intent(context, SurveyService.class);
		intent.setAction(ACTION_INSERT);

		ContentValues cv = new ContentValues();
		cv.put(FixmeTableMetaData.LATITUDE, lat);
		cv.put(FixmeTableMetaData.LONGITUDE, lng);
		cv.put(FixmeTableMetaData.COMMENT, comment);

		intent.setData(FixmeTableMetaData.CONTENT_URI);
		intent.putExtra(EXTRA_CONTENT_VALUE, cv);
		context.startService(intent);
	}

	public static void startDeleteFixme(Context context) {
		startDelete(context, FixmeTableMetaData.CONTENT_URI);
	}


	public SurveyService() {
		super("SurveyService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			if (ACTION_INSERT.equals(action)) {
				Uri uri = intent.getData();
				ContentValues cv = intent.getParcelableExtra(EXTRA_CONTENT_VALUE);
				handleInsert(uri, cv);
			} else if (ACTION_DELETE.equals(action)) {
				Uri uri = intent.getData();
				handleDelete(uri);
			}
		}
	}

	private void handleInsert(Uri uri, ContentValues cv) {
		ContentResolver cr = getContentResolver();
		cr.insert(uri, cv);
	}

	private void handleDelete(Uri uri) {
		ContentResolver cr = getContentResolver();
		cr.delete(uri, null, null);
	}
}
