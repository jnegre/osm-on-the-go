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

import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.FixmeTableMetaData;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class FixmeOverlay extends Overlay implements LoaderManager.LoaderCallbacks<Cursor> {

	private final static String TAG = "FixmeOverlay";

	private final Paint paint;
	private final Context context;
	private final MapView mapView;
	private Cursor cursor;

	public FixmeOverlay(Context ctx, MapView mapView) {
		super(ctx);
		this.context = ctx;
		this.mapView = mapView;

		this.paint = new Paint();
		this.paint.setColor(Color.RED);
		this.paint.setAntiAlias(true);
		this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
		this.paint.setAlpha(255);
		this.paint.setStrokeWidth(0.5f * mScale);
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
			int iLat = cursor.getColumnIndex(FixmeTableMetaData.LATITUDE);
			int iLong = cursor.getColumnIndex(FixmeTableMetaData.LONGITUDE);

			//walk through the rows based on indexes
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				//Gather values
				double lat = cursor.getDouble(iLat);
				double lng = cursor.getDouble(iLong);

				IGeoPoint geoPoint = new GeoPoint(lat, lng);

				pj.toPixels(geoPoint, point);

				c.drawCircle(point.x, point.y, 5 * mScale, paint);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		Log.d(TAG, "onCreateLoader");
		return new CursorLoader(
				context,
				FixmeTableMetaData.CONTENT_URI,
				new String[]{FixmeTableMetaData.LATITUDE, FixmeTableMetaData.LONGITUDE},
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
