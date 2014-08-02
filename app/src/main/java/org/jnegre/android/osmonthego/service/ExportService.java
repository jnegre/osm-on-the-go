package org.jnegre.android.osmonthego.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;
import org.jnegre.android.osmonthego.R;
import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.AddressTableMetaData;
import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.FixmeTableMetaData;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ExportService extends IntentService {
	private final static String TAG = "ExportService";

	private final static String ACTION_EXPORT_OSM = "org.jnegre.android.osmonthego.service.action.EXPORT_OSM";
    private final static String EXTRA_INCLUDE_ADDRESS = "org.jnegre.android.osmonthego.service.extra.INCLUDE_ADDRESS";
    private final static String EXTRA_INCLUDE_FIXME = "org.jnegre.android.osmonthego.service.extra.INCLUDE_FIXME";

	private final static double MARGIN = 0.001;

	private final Handler handler;

	public static void startOsmExport(Context context, boolean includeAddress, boolean includeFixme) {
        Intent intent = new Intent(context, ExportService.class);
        intent.setAction(ACTION_EXPORT_OSM);
        intent.putExtra(EXTRA_INCLUDE_ADDRESS, includeAddress);
		intent.putExtra(EXTRA_INCLUDE_FIXME, includeFixme);
        context.startService(intent);
    }

    public ExportService() {
        super("ExportService");
		this.handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_EXPORT_OSM.equals(action)) {
				boolean includeAddress = intent.getBooleanExtra(EXTRA_INCLUDE_ADDRESS, false);
				boolean includeFixme = intent.getBooleanExtra(EXTRA_INCLUDE_FIXME, false);
				handleOsmExport(includeAddress, includeFixme);
            }
        }
    }

    /**
     * Handle export in the provided background thread
     */
    private void handleOsmExport(boolean includeAddress, boolean includeFixme) {
		//TODO handle empty survey
		//TODO handle bounds around +/-180

		if(!isExternalStorageWritable()) {
			notifyUserOfError();
			return;
		}

		int id = 0;
		double minLat = 200;
		double minLng = 200;
		double maxLat = -200;
		double maxLng = -200;
		StringBuilder builder = new StringBuilder();

		if (includeAddress) {
			Uri uri = AddressTableMetaData.CONTENT_URI;
			Cursor cursor = getContentResolver().query(uri,
					new String[]{ //projection
							AddressTableMetaData.LATITUDE,
							AddressTableMetaData.LONGITUDE,
							AddressTableMetaData.NUMBER,
							AddressTableMetaData.STREET},
					null, //selection string
					null, //selection args array of strings
					null); //sort order

			if(cursor == null) {
				notifyUserOfError();
				return;
			}

			try {
				int iLat = cursor.getColumnIndex(AddressTableMetaData.LATITUDE);
				int iLong = cursor.getColumnIndex(AddressTableMetaData.LONGITUDE);
				int iNumber = cursor.getColumnIndex(AddressTableMetaData.NUMBER);
				int iStreet = cursor.getColumnIndex(AddressTableMetaData.STREET);

				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					//Gather values
					double lat = cursor.getDouble(iLat);
					double lng = cursor.getDouble(iLong);
					String number = cursor.getString(iNumber);
					String street = cursor.getString(iStreet);

					minLat = Math.min(minLat, lat);
					maxLat = Math.max(maxLat, lat);
					minLng = Math.min(minLng, lng);
					maxLng = Math.max(maxLng, lng);
					builder.append("<node id=\"-")
							.append(++id)
							.append("\" lat=\"")
							.append(lat)
							.append("\" lon=\"")
							.append(lng)
							.append("\" version=\"1\" action=\"modify\">\n");
					addOsmTag(builder, "addr:housenumber", number);
					addOsmTag(builder, "addr:street", street);
					builder.append("</node>\n");
				}
			} finally {
				cursor.close();
			}
		}

		if (includeFixme) {
			Uri uri = FixmeTableMetaData.CONTENT_URI;
			Cursor cursor = getContentResolver().query(uri,
					new String[]{ //projection
							FixmeTableMetaData.LATITUDE,
							FixmeTableMetaData.LONGITUDE,
							FixmeTableMetaData.COMMENT},
					null, //selection string
					null, //selection args array of strings
					null); //sort order

			if(cursor == null) {
				notifyUserOfError();
				return;
			}

			try {
				int iLat = cursor.getColumnIndex(FixmeTableMetaData.LATITUDE);
				int iLong = cursor.getColumnIndex(FixmeTableMetaData.LONGITUDE);
				int iComment = cursor.getColumnIndex(FixmeTableMetaData.COMMENT);

				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					//Gather values
					double lat = cursor.getDouble(iLat);
					double lng = cursor.getDouble(iLong);
					String comment = cursor.getString(iComment);

					minLat = Math.min(minLat, lat);
					maxLat = Math.max(maxLat, lat);
					minLng = Math.min(minLng, lng);
					maxLng = Math.max(maxLng, lng);
					builder.append("<node id=\"-")
							.append(++id)
							.append("\" lat=\"")
							.append(lat)
							.append("\" lon=\"")
							.append(lng)
							.append("\" version=\"1\" action=\"modify\">\n");
					addOsmTag(builder, "fixme", comment);
					builder.append("</node>\n");
				}
			} finally {
				cursor.close();
			}
		}

		try {
			File destinationFile = getDestinationFile();
			destinationFile.getParentFile().mkdirs();
			PrintWriter writer = new PrintWriter(destinationFile, "UTF-8");

			writer.println("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
			writer.println("<osm version=\"0.6\" generator=\"OsmOnTheGo\">");
			writer.print("<bounds minlat=\"");
			writer.print(minLat - MARGIN);
			writer.print("\" minlon=\"");
			writer.print(minLng - MARGIN);
			writer.print("\" maxlat=\"");
			writer.print(maxLat + MARGIN);
			writer.print("\" maxlon=\"");
			writer.print(maxLng + MARGIN);
			writer.println("\" />");

			writer.println(builder);

			writer.print("</osm>");
			writer.close();

			if(writer.checkError()) {
				notifyUserOfError();
			} else {
				//FIXME i18n the subject and content
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				emailIntent.setType(HTTP.OCTET_STREAM_TYPE);
				//emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"johndoe@exemple.com"});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OSM On The Go");
				emailIntent.putExtra(Intent.EXTRA_TEXT, "Your last survey.");
				emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(destinationFile));
				startActivity(emailIntent);
			}
		} catch (IOException e) {
			Log.e(TAG,"Could not write to file", e);
			notifyUserOfError();
		}

	}

	private void notifyUserOfError() {
		handler.post(new Runnable() {
						 @Override
						 public void run() {
							 Toast.makeText(getApplicationContext(), R.string.msg_export_failed, Toast.LENGTH_SHORT).show();
						 }
					 });
	}

	private static void addOsmTag(StringBuilder builder, String k, String v) {
		if (v != null) {
			builder.append("<tag k=\"").append(k).append("\" v=\"").append(v).append("\" />\n");
		}
	}

	private File getDestinationFile() {
		File externalStorage = Environment.getExternalStorageDirectory();
		String dirName = getApplicationInfo().packageName;
		File dir = new File(externalStorage, dirName);
		//TODO put date-hour-minute in the file name
		return new File(dir, "onthego.osm");
	}

	/* Checks if external storage is available for read and write */
	private static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

}
