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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public class ControlOverlay extends Overlay {

	Paint barPaint;
	int unit = 5;

	public ControlOverlay(Context ctx) {
		super(ctx);
		final DisplayMetrics dm = ctx.getResources().getDisplayMetrics();

		this.barPaint = new Paint();
		this.barPaint.setColor(Color.BLACK);
		this.barPaint.setAntiAlias(true);
		this.barPaint.setStyle(Paint.Style.STROKE);
		this.barPaint.setAlpha(100);
		this.barPaint.setStrokeWidth(1.5f * dm.density);

	}

	@Override
	protected void draw(Canvas c, MapView osmv, boolean shadow) {
		float cx = c.getWidth() / 2f;
		float cy = c.getHeight() / 2f;

		c.drawCircle(cx, cy, 2 * unit, barPaint);
		c.drawLine(cx + unit, cy + unit, cx - unit, cy - unit, barPaint);
		c.drawLine(cx + unit, cy - unit, cx - unit, cy + unit, barPaint);
	}
}
