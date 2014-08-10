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

package org.jnegre.android.osmonthego.osmdroid;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.jnegre.android.osmonthego.provider.SurveyProviderMetaData.FixmeTableMetaData;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class FixmeOverlay extends Overlay implements LoaderManager.LoaderCallbacks<Cursor>, ItemSelector {

	private final static String TAG = "FixmeOverlay";

	private final Paint paint;
	private final Context context;
	private final MapView mapView;
	private Cursor cursor;
	private Uri selectedItem;

	public FixmeOverlay(Context ctx, MapView mapView) {
		super(ctx);
		this.context = ctx;
		this.mapView = mapView;

		this.paint = new Paint();
		this.paint.setColor(Color.RED);
		this.paint.setAntiAlias(true);
		this.paint.setStyle(Paint.Style.FILL);
		this.paint.setAlpha(255);
		//this.paint.setStrokeWidth(0.5f * mScale);
	}

	@Override
	protected void draw(final Canvas c, MapView mapView, boolean shadow) {
		if (!isEnabled() || shadow) {
			return;
		}

		selectedItem = null;

		if(cursor != null) {
			int iId = cursor.getColumnIndex(FixmeTableMetaData._ID);
			int iLat = cursor.getColumnIndex(FixmeTableMetaData.LATITUDE);
			int iLong = cursor.getColumnIndex(FixmeTableMetaData.LONGITUDE);

			final Projection pj = mapView.getProjection();
			final Point point = new Point();
			final Rect bounds = new Rect();
			float canvasCenterX = c.getWidth()/2f;
			float canvasCenterY = c.getHeight()/2f;
			final float radius =  5 * mScale;
			final float radiusSquare = radius * radius;

			//walk through the rows based on indexes
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				//Gather values
				double lat = cursor.getDouble(iLat);
				double lng = cursor.getDouble(iLong);

				IGeoPoint geoPoint = new GeoPoint(lat, lng);

				pj.toPixels(geoPoint, point);

				c.drawCircle(point.x, point.y, radius, paint);


				//Is it at the center of the map?
				if((point.x-canvasCenterX)*(point.x-canvasCenterX)+(point.y-canvasCenterY)*(point.y-canvasCenterY)<radiusSquare) {
					selectedItem = ContentUris.withAppendedId(FixmeTableMetaData.CONTENT_URI, cursor.getLong(iId));
				}

			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		Log.d(TAG, "onCreateLoader");
		return new CursorLoader(
				context,
				FixmeTableMetaData.CONTENT_URI,
				new String[]{FixmeTableMetaData._ID, FixmeTableMetaData.LATITUDE, FixmeTableMetaData.LONGITUDE},
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

	@Override
	public Uri getSelectedItem() {
		return isEnabled()?selectedItem:null;
	}
}
