package org.jnegre.android.osmonthego.osmdroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import org.jnegre.android.osmonthego.AddressIterator;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class AddressOverlay extends Overlay {

	private final static String TAG = "AddressOverlay";

	private final Paint numberPaint;
	private final Paint backPaint;
	private final Context context;

	public AddressOverlay(Context ctx) {
		super(ctx);
		this.context = ctx;
		final DisplayMetrics dm = ctx.getResources().getDisplayMetrics();

		this.numberPaint = new Paint();
		this.numberPaint.setColor(Color.WHITE);
		this.numberPaint.setAntiAlias(true);
		this.numberPaint.setStyle(Paint.Style.FILL);
		this.numberPaint.setTextSize(12 * dm.density);
		this.numberPaint.setAlpha(255);
		this.numberPaint.setStrokeWidth(0.5f * dm.density);

		this.backPaint = new Paint();
		this.backPaint.setColor(Color.BLUE);
		this.backPaint.setAntiAlias(true);
		this.backPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		this.backPaint.setAlpha(255);
		this.backPaint.setStrokeWidth(4 * dm.density);

	}

	@Override
	protected void draw(final Canvas c, MapView mapView, boolean shadow) {
		if (!isEnabled() || shadow) {
			return;
		}

		final Projection pj = mapView.getProjection();
		final Point point = new Point();
		final Rect bounds = new Rect();

		new AddressIterator(context) {
			@Override
			protected void onAddress(double lat, double lng, String number, String street) {
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
		}.iterate();
	}
}
