package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by jeremiahstephenson on 10/7/13.
 */
public class MeterMarkerLineGraphView extends LineGraphView {

    private Paint marker;
    private boolean showMarkers = true;
    private boolean isMetric = true;

    // one mile in meters
    public static final double ONE_MILE = 1609.34;

    // one kilometer in meters
    public static final double ONE_KILOMETER = 1000;

    public MeterMarkerLineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupMarkerPaint();
    }

    public MeterMarkerLineGraphView(Context context, AttributeSet attrs, boolean verticalLabelsOnRight) {
        super(context, attrs, verticalLabelsOnRight);
        setupMarkerPaint();
    }

    public MeterMarkerLineGraphView(Context context, String title) {
        super(context, title);
        setupMarkerPaint();
    }

    public MeterMarkerLineGraphView(Context context, String title, boolean verticalLabelsOnRight) {
        super(context, title, verticalLabelsOnRight);
        setupMarkerPaint();
    }

    @Override
    /**
     * This is assuming that the data being sent in is in meters
     */
    public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeries.GraphViewSeriesStyle style, int[] colors) {

        super.drawSeries(canvas, values, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style, colors);

        if (showMarkers) {

            final double conversion = isMetric ? ONE_KILOMETER : ONE_MILE;
            double diff = 0;

            for (int i = 0; i < values.length; i++) {

                if (i + 1 < values.length) {

                    final double x1 = values[i].getX();
                    final double x2 = values[i + 1].getX();

                    final double slope = (values[i + 1].getY() - values[i].getY()) / (values[i + 1].getX() - values[i].getX());

                    diff = x1 + (conversion - (x1 % conversion));

                    if (diff <= x2) {

                        do {

                            final String lap = String.valueOf((int)(diff / conversion));
                            final float pointX = (float) (graphwidth * ((diff - minX) / diffX)) + (horstart + 1);
                            final float pointY = (float) (border - (graphheight * (((slope * (diff - values[i].getX())) + values[i].getY() - minY) / diffY))) + graphheight;

                            marker.setColor(0x64ffffff);

                            canvas.drawCircle(pointX, pointY, 20, marker);

                            marker.setColor(0xc80096d6);

                            canvas.drawCircle(pointX, pointY, 15, marker);

                            marker.setColor(0xffffffff);
                            marker.setTextSize(20);

                            final Rect textBounds = new Rect();
                            marker.getTextBounds(lap, 0, lap.length(), textBounds);

                            canvas.drawText(lap, pointX - (textBounds.width() / 2) - textBounds.left, pointY + (textBounds.height() / 2), marker);

                            diff += conversion;

                        } while (diff <= x2);
                    }
                }
            }

            final double maxX = getMaxX(false);
            if (diff <= maxX) {
                do {

                    final String lap = String.valueOf((int)(diff / conversion));
                    final float pointX = (float) (graphwidth * ((diff - minX) / diffX)) + (horstart + 1);
                    final float pointY = graphheight + border;

                    marker.setColor(0x64ffffff);

                    canvas.drawCircle(pointX, pointY, 20, marker);

                    marker.setColor(0xffb2b2b2);

                    canvas.drawCircle(pointX, pointY, 15, marker);

                    marker.setColor(0xffffffff);
                    marker.setTextSize(20);

                    final Rect textBounds = new Rect();
                    marker.getTextBounds(lap, 0, lap.length(), textBounds);

                    canvas.drawText(lap, pointX - (textBounds.width() / 2) - textBounds.left, pointY + (textBounds.height() / 2), marker);

                    diff += conversion;

                } while (diff <= maxX);
            }
        }
    }

    public void setShowMarkers(boolean show) {
        this.showMarkers = show;
    }

    public void setIsMetric(boolean metric) {
        this.isMetric = metric;
    }

    private void setupMarkerPaint() {
        marker = new Paint();
    }
}
