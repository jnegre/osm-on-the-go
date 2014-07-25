package org.jnegre.android.osmonthego.osmdroid;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.AddressTableMetaData;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class AddressOverlay extends Overlay implements LoaderManager.LoaderCallbacks<Cursor> {

	private final static String TAG = "AddressOverlay";

	private final Paint numberPaint;
	private final Paint backPaint;
	private final Context context;
	private final MapView mapView;
	private Cursor cursor;

	public AddressOverlay(Context ctx, MapView mapView) {
		super(ctx);
		this.context = ctx;
		this.mapView = mapView;

		this.numberPaint = new Paint();
		this.numberPaint.setColor(Color.WHITE);
		this.numberPaint.setAntiAlias(true);
		this.numberPaint.setStyle(Paint.Style.FILL);
		this.numberPaint.setTextSize(12 * mScale);
		this.numberPaint.setAlpha(255);
		this.numberPaint.setStrokeWidth(0.5f * mScale);

		this.backPaint = new Paint();
		this.backPaint.setColor(Color.BLUE);
		this.backPaint.setAntiAlias(true);
		this.backPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		this.backPaint.setAlpha(255);
		this.backPaint.setStrokeWidth(4 * mScale);

	}

	@Override
	protected void draw(final Canvas c, MapView mapView, boolean shadow) {
		if (!isEnabled() || shadow) {
			return;
		}

		final Projection pj = mapView.getProjection();
		final Point point = new Point();
		final Rect bounds = new Rect();

		if(cursor != null) {
			int iLat = cursor.getColumnIndex(AddressTableMetaData.LATITUDE);
			int iLong = cursor.getColumnIndex(AddressTableMetaData.LONGITUDE);
			int iNumber = cursor.getColumnIndex(AddressTableMetaData.NUMBER);

			//walk through the rows based on indexes
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				//Gather values
				double lat = cursor.getDouble(iLat);
				double lng = cursor.getDouble(iLong);
				String number = cursor.getString(iNumber);

				IGeoPoint geoPoint = new GeoPoint(lat, lng);

				pj.toPixels(geoPoint, point);

				numberPaint.getTextBounds(number, 0, number.length(), bounds);

				float centerX = point.x - bounds.exactCenterX();
				float centerY = point.y - bounds.exactCenterY();

				//FIXME debug
				//c.drawLine(point.x - 20, point.y, point.x + 20, point.y, backPaint);
				//c.drawLine(point.x, point.y - 20, point.x, point.y + 20, backPaint);

				c.drawRect(point.x - bounds.width() / 2, point.y - bounds.height() / 2, point.x + bounds.width() / 2, point.y + bounds.height() / 2, backPaint);
				c.drawText(number, centerX, centerY, numberPaint);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		Log.d(TAG, "onCreateLoader");
		return new CursorLoader(
				context,
				AddressTableMetaData.CONTENT_URI,
				new String[]{AddressTableMetaData.LATITUDE, AddressTableMetaData.LONGITUDE, AddressTableMetaData.NUMBER},
				null,
				null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		Log.d(TAG, "onLoadFinished");
		this.cursor = cursor;
		mapView.invalidate();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		Log.d(TAG, "onLoaderReset");
		this.cursor = null;
	}
}
