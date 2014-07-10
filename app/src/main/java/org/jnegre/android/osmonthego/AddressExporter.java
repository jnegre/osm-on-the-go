package org.jnegre.android.osmonthego;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class AddressExporter {

	private final static String TAG = "AddressExporter";
	private final static double MARGIN = 0.001;

	private final Context context;

	public AddressExporter(Context context) {
		this.context = context;
	}

	/* Checks if external storage is available for read and write */
	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}


	public File getDestinationFile() {
		File externalStorage = Environment.getExternalStorageDirectory();
		String dirName = context.getApplicationInfo().packageName;
		File dir = new File(externalStorage, dirName);
		return new File(dir, "onthego.osm");
	}

	public boolean export() {
		//TODO handle empty set
		if (isExternalStorageWritable()) {
			File destinationFile = getDestinationFile();
			destinationFile.getParentFile().mkdirs();
			try {
				final PrintWriter writer = new PrintWriter(destinationFile, "UTF-8");

				new AddressIterator(context) {

					private int id;
					private double minLat = 200;
					private double minLng = 200;
					private double maxLat = -200;
					private double maxLng = -200;
					private StringBuilder builder = new StringBuilder();

					protected void onAfterAddresses() {
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
					}

					private void addTag(String k, String v) {
						if (v != null) {
							builder.append("<tag k=\"").append(k).append("\" v=\"").append(v).append("\" />\n");
						}
					}

					@Override
					protected void onAddress(double lat, double lng, String number, String street) {
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
						addTag("addr:housenumber", number);
						addTag("addr:street", street);
						builder.append("</node>\n");
					}
				}.iterate();
				writer.close();
				return !writer.checkError();
			} catch (IOException e) {
				Log.e(TAG, "Could not write to file", e);
				return false;
			}
		} else {
			return false;
		}
	}

}
