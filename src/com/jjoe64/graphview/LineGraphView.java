/**
 * This file is part of GraphView.
 *
 * GraphView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GraphView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GraphView.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 *
 * Copyright Jonas Gehring
 */

package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * Line Graph View. This draws a line chart.
 */
public class LineGraphView extends GraphView {
	private final Paint paintBackground;
	private boolean drawBackground;
    private Path poly;

    public LineGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, false);
    }

	public LineGraphView(Context context, AttributeSet attrs, boolean verticalLabelsOnRight) {
		super(context, attrs, verticalLabelsOnRight);

		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
	}

    public LineGraphView(Context context, String title) {
        this(context, title, false);
    }

	public LineGraphView(Context context, String title, boolean verticalLabelsOnRight) {
		super(context, title, verticalLabelsOnRight);

		paintBackground = new Paint();
		paintBackground.setColor(Color.rgb(20, 40, 60));
		paintBackground.setStrokeWidth(4);
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeriesStyle style, int[] colors) {

		double lastEndY = 0;
		double lastEndX = 0;

        // draw background
        drawShadedBackground(canvas, values, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart);

        final Shader temp = paint.getShader();

		// draw data
		paint.setStrokeWidth(style.thickness);
        if (colors != null) {
            paint.setShader(new LinearGradient(0, 0, 0, getHeight(), colors, null, Shader.TileMode.MIRROR));
        } else {
            paint.setColor(style.color);
        }

		for (int i = 0; i < values.length; i++) {
			double valY = values[i].getY() - minY;
			double ratY = valY / diffY;
			double y = graphheight * ratY;

			double valX = values[i].getX() - minX;
			double ratX = valX / diffX;
			double x = graphwidth * ratX;

			if (i > 0) {
				float startX = (float) lastEndX + (horstart + 1);
				float startY = (float) (border - lastEndY) + graphheight;
				float endX = (float) x + (horstart + 1);
				float endY = (float) (border - y) + graphheight;

				canvas.drawLine(startX, startY, endX, endY, paint);
			}
			lastEndY = y;
			lastEndX = x;
		}

        paint.setShader(temp);
	}

	public int getBackgroundColor() {
		return paintBackground.getColor();
	}

	public boolean getDrawBackground() {
		return drawBackground;
	}

	@Override
	public void setBackgroundColor(int color) {
		paintBackground.setColor(color);
	}

	/**
	 * @param drawBackground true for a light blue background under the graph line
	 */
	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

    protected void drawShadedBackground(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart) {

        if (drawBackground) {

            if (poly == null) {
                poly = new Path();
            }

            poly.reset();

            float startY = graphheight + border;
            float initialX = 0;
            float initialY = 0;
            float endX = 0;
            float endY;
            double x;
            double y;
            float tempX;

            for (int i = 0; i < values.length; i++) {

                y = graphheight * ((values[i].getY() - minY) / diffY);
                x = graphwidth * ((values[i].getX() - minX) / diffX);

                tempX = (float)x;
                endX = tempX + (horstart + 1);
                endY = (float) (border - y) + graphheight + 2;

                if (i == 0) {
                    initialX = endX;
                    initialY = endY;
                    poly.moveTo(endX, endY);
                } else {
                    poly.lineTo(endX, endY);
                }
            }

            poly.lineTo(endX, startY);
            poly.lineTo(initialX, startY);
            poly.lineTo(initialX, initialY);
            poly.close();

            canvas.drawPath(poly, paintBackground);
        }
    }
}
