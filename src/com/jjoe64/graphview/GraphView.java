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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.compatible.ScaleGestureDetector;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs.
 * This is the abstract base class for all graphs. Extend this class and implement {@link ##drawSeries(Canvas, GraphViewDataInterface[], float, float, float, double, double, double, double, float)} to display a custom graph.
 * Use {@link LineGraphView} for creating a line chart.
 *
 * @author jjoe64 - jonas gehring - http://www.jjoe64.com
 *
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 */
abstract public class GraphView extends RelativeLayout {
	static final private class GraphViewConfig {
		static final float BORDER = 25;
        static final float SIDE_BORDER = 10;
	}

	private class GraphViewContentView extends View {
		private float lastTouchEventX;
		private float graphwidth;
		private boolean scrollingStarted;
        private boolean showOnLeft = false;
        private boolean showSideImages = false;

		/**
		 * @param context
		 */
		public GraphViewContentView(Context context, boolean showOnLeft, boolean showSideImages) {
			super(context);
            this.showOnLeft = showOnLeft;
            this.showSideImages = showSideImages;
			setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {

            paint.setAntiAlias(true);

            // normal
            paint.setStrokeWidth(0);

            float border = GraphViewConfig.BORDER;
            float horstart = showOnLeft ? 0 : (viewVerLabels.getLayoutParams().width +
                    ((LayoutParams)viewVerLabels.getLayoutParams()).leftMargin +
                    ((LayoutParams)viewVerLabels.getLayoutParams()).rightMargin);

            horstart += (showOnLeft && showSideImages) ? (viewVerImages.getLayoutParams().width +
                    ((LayoutParams)viewVerImages.getLayoutParams()).leftMargin +
                    ((LayoutParams)viewVerImages.getLayoutParams()).rightMargin) : 0;

            float height = getHeight();
            float width = getWidth() - (viewVerLabels.getLayoutParams().width +
                    ((LayoutParams)viewVerLabels.getLayoutParams()).leftMargin +
                    ((LayoutParams)viewVerLabels.getLayoutParams()).rightMargin) - 1;

            width -= showSideImages ? ((viewVerImages.getLayoutParams().width +
                    ((LayoutParams)viewVerImages.getLayoutParams()).leftMargin +
                    ((LayoutParams)viewVerImages.getLayoutParams()).rightMargin) - 1) : 0;

            double maxY = getMaxY();
            double minY = getMinY();
            double maxX = getMaxX(false);
            double minX = getMinX(false);
            double diffX = maxX - minX;

             // measure bottom text
            if (labelTextHeight == null || horLabelTextWidth == null) {
                paint.setTextSize(getGraphViewStyle().getTextSize());
                double testX = ((getMaxX(true)-getMinX(true))*0.783)+getMinX(true);
                String testLabel = formatLabel(testX, true);
                paint.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
                labelTextHeight = (textBounds.height());
                horLabelTextWidth = (textBounds.width());
            }
            border += labelTextHeight;

            float graphheight = height - (2 * border);
            graphwidth = width;

            if (horlabels == null) {
                horlabels = generateHorlabels(graphwidth);
            }
            if (verlabels == null) {
                verlabels = generateVerlabels(graphheight);
            }

            // horizontal lines
            paint.setTextAlign(Align.LEFT);
            int vers = verlabels.length - 1;
            for (int i = 0; i < verlabels.length; i++) {
                paint.setColor(graphViewStyle.getGridColor());
                float y = ((graphheight / vers) * i) + border;
                if ((i != verlabels.length - 1) || getGraphViewStyle().getShowBottomLinesAndLabels()) {
                    canvas.drawLine(horstart, y, width + horstart, y, paint);
                }
            }

            // horizontal labels + vertical lines
            int hors = horlabels.length - 1;
            for (int i = 0; i < horlabels.length; i++) {
                paint.setColor(graphViewStyle.getGridColor());
                float x = ((graphwidth / hors) * i) + horstart;
                if (showVerticalGridLines) {
                    canvas.drawLine(x, height - border, x, border, paint);
                }
                paint.setTextAlign(Align.CENTER);
                if (i==horlabels.length-1)
                    paint.setTextAlign(Align.RIGHT);
                if (i==0)
                    paint.setTextAlign(Align.LEFT);
                paint.setColor(graphViewStyle.getHorizontalLabelsColor());

                if (getGraphViewStyle().getShowBottomLinesAndLabels()) {
                    canvas.drawText(horlabels[i], x, height - 4, paint);
                }
            }

            paint.setTextAlign(Align.CENTER);
            canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

            if (maxY == minY) {
                // if min/max is the same, fake it so that we can render a line
                if(maxY == 0) {
                    // if both are zero, change the values to prevent division by zero
                    maxY = 1.0d;
                    minY = 0.0d;
                } else {
                    maxY = maxY*1.05d;
                    minY = minY*0.95d;
                }
            }

            double diffY = maxY - minY;
            paint.setStrokeCap(Paint.Cap.ROUND);

            for (int i=0; i<graphSeries.size(); i++) {
                drawSeries(canvas, _values(i), graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, graphSeries.get(i).style, getGraphViewStyle().getLineGradientColors());
            }

            if (showLegend) drawLegend(canvas, height, width);
		}

		private void onMoveGesture(float f) {
			// view port update
			if (viewportSize != 0) {
				viewportStart -= f*viewportSize/graphwidth;

				// minimal and maximal view limit
				//double minX = getMinX(true);
				double maxX = getMaxX(true);

//				if (viewportStart < minX) {
//					viewportStart = minX;
//				} else if (viewportStart+viewportSize > maxX) {
//					viewportStart = maxX - viewportSize;
//				}

                if (viewportStart+viewportSize > (maxX + scrollPaddingRight)) {
                    viewportStart = (maxX + scrollPaddingRight) - viewportSize;
                }

                if (viewportStart < viewportStartInit - scrollPaddingLeft) {
                    viewportStart = viewportStartInit - scrollPaddingLeft;
                }

				// labels have to be regenerated
				if (!staticHorizontalLabels) horlabels = null;
				if (!staticVerticalLabels) verlabels = null;
				viewVerLabels.invalidate();

                if (viewVerImages != null) {
                    viewVerImages.invalidate();
                }
			}
			invalidate();
		}

		/**
		 * @param event
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (!isScrollable() || isDisableTouch()) {
				return super.onTouchEvent(event);
			}

			boolean handled = false;
			// first scale
			if (scalable && scaleDetector != null) {
				scaleDetector.onTouchEvent(event);
				handled = scaleDetector.isInProgress();
			}
			if (!handled) {
				//Log.d("GraphView", "on touch event scale not handled+"+lastTouchEventX);
				// if not scaled, scroll
				if ((event.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN) {
					scrollingStarted = true;
					handled = true;
				}
				if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
					scrollingStarted = false;
					lastTouchEventX = 0;
					handled = true;
				}
				if ((event.getAction() & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE) {
					if (scrollingStarted) {
						if (lastTouchEventX != 0) {
							onMoveGesture(event.getX() - lastTouchEventX);
						}
						lastTouchEventX = event.getX();
						handled = true;
					}
				}
				if (handled)
					invalidate();
			} else {
				// currently scaling
				scrollingStarted = false;
				lastTouchEventX = 0;
			}
			return handled;
		}
	}

	/**
	 * one data set for a graph series
	 */
	static public class GraphViewData implements GraphViewDataInterface, Serializable {
		public final double valueX;
		public final double valueY;
		public GraphViewData(double valueX, double valueY) {
			super();
			this.valueX = valueX;
			this.valueY = valueY;
		}
		@Override
		public double getX() {
			return valueX;
		}
		@Override
		public double getY() {
			return valueY;
		}
	}

	public enum LegendAlign {
		TOP, MIDDLE, BOTTOM
	}

    private class VerImagesView extends View {

        public VerImagesView(Context context) {
            super(context);
        }

        public VerImagesView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public VerImagesView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public VerImagesView(Context context, boolean verticalImagesOnRight) {
            super(context);

            final LayoutParams params = new LayoutParams(
                    getGraphViewStyle().getVerticalImagesWidth() == 0 ? 100 : getGraphViewStyle().getVerticalImagesWidth()
                    , LayoutParams.MATCH_PARENT);

            params.addRule(verticalImagesOnRight ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);

            setLayoutParams(params);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            measureBottomText();

            float border = GraphViewConfig.BORDER;
            border += labelTextHeight;
            float height = getHeight();
            float graphheight = height - (2 * border);

            if (verImages == null) {
                verImages = getVerImages();
            }

            if (verImagesWidth == null) {
                int testWidth = 0;
                for (Bitmap test : verImages) {
                    if (test != null && test.getWidth() > testWidth) {
                        testWidth = test.getWidth();
                    }
                }
                verImagesWidth = testWidth;
            }

            if (getGraphViewStyle().getVerticalImagesWidth()==0 && getLayoutParams().width != verImagesWidth+GraphViewConfig.SIDE_BORDER) {
                setViewLayoutParams(this, (int)(verImagesWidth+GraphViewConfig.SIDE_BORDER), LayoutParams.MATCH_PARENT,
                        getGraphViewStyle().getVerticalImagesLeftMargin(), getGraphViewStyle().getVerticalImagesRightMargin());

            } else if (getGraphViewStyle().getVerticalImagesWidth()!=0 && getGraphViewStyle().getVerticalImagesWidth() != getLayoutParams().width) {
                setViewLayoutParams(this, getGraphViewStyle().getVerticalImagesWidth(), LayoutParams.MATCH_PARENT,
                        getGraphViewStyle().getVerticalImagesLeftMargin(), getGraphViewStyle().getVerticalImagesRightMargin());
            }

            int vers = verImages.length - 1;
            for (int i = 0; i < verImages.length; i++) {
                float y = ((graphheight / vers) * i) + border;
                if (verImages[i] != null && (i < verImages.length - 1 && !getGraphViewStyle().getShowBottomLinesAndLabels()) || getGraphViewStyle().getShowBottomLinesAndLabels()) {
                    canvas.drawBitmap(verImages[i], 0, y - verImages[i].getHeight(), paint);
                }
            }

        }
    }

	private class VerLabelsView extends View {
		/**
		 * @param context
		 */
		public VerLabelsView(Context context, boolean verticalLabelsOnRight) {
			super(context);

            final LayoutParams params = new LayoutParams(
                    getGraphViewStyle().getVerticalLabelsWidth() == 0 ? 100 : getGraphViewStyle().getVerticalLabelsWidth()
                    , LayoutParams.MATCH_PARENT);

            params.addRule(verticalLabelsOnRight ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);

			setLayoutParams(params);
		}

		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {
			// normal
			paint.setStrokeWidth(0);

			 // measure bottom text
			measureBottomText();

			float border = GraphViewConfig.BORDER;
			border += labelTextHeight;
			float height = getHeight();
			float graphheight = height - (2 * border);

			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

            if (verLabelTextWidth == null) {
                int testWidth = 0;
                for (String test : verlabels) {
                    paint.getTextBounds(test, 0, test.length(), textBounds);

                    if (textBounds.width() > testWidth) {
                        testWidth = textBounds.width();
                    }
                }
                verLabelTextWidth = testWidth;
            }

            if (getGraphViewStyle().getVerticalLabelsWidth()==0 && getLayoutParams().width != verLabelTextWidth+GraphViewConfig.SIDE_BORDER) {
//				setLayoutParams(new LayoutParams(
//						(int) (verLabelTextWidth+GraphViewConfig.BORDER), LayoutParams.FILL_PARENT));
                setViewLayoutParams(this, (int)(verLabelTextWidth+GraphViewConfig.SIDE_BORDER), LayoutParams.MATCH_PARENT,
                        getGraphViewStyle().getVerticalLabelsLeftMargin(), getGraphViewStyle().getVerticalLabelsRightMargin());

            } else if (getGraphViewStyle().getVerticalLabelsWidth()!=0 && getGraphViewStyle().getVerticalLabelsWidth() != getLayoutParams().width) {
//				setLayoutParams(new LayoutParams(
//						getGraphViewStyle().getVerticalLabelsWidth(), LayoutParams.FILL_PARENT));
                setViewLayoutParams(this, getGraphViewStyle().getVerticalLabelsWidth(), LayoutParams.MATCH_PARENT,
                        getGraphViewStyle().getVerticalLabelsLeftMargin(), getGraphViewStyle().getVerticalLabelsRightMargin());
            }

			// vertical labels
			paint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				float y = ((graphheight / vers) * i) + border;
				paint.setColor(graphViewStyle.getVerticalLabelsColor());
                if ((i < verlabels.length - 1 && !getGraphViewStyle().getShowBottomLinesAndLabels()) || getGraphViewStyle().getShowBottomLinesAndLabels()) {
				    canvas.drawText(verlabels[i], 0, y, paint);
                }
			}
		}
	}

	protected final Paint paint;
	private String[] horlabels;
	private String[] verlabels;
    private Bitmap[] verImages;
	private String title;
	private boolean scrollable;
	private boolean disableTouch;
    private boolean showVerticalGridLines = true;
	private double viewportStart;
    private double viewportStartInit;
    private double scrollPaddingLeft;
    private double scrollPaddingRight;
	private double viewportSize;
	private final View viewVerLabels;
    private View viewVerImages = null;
	private ScaleGestureDetector scaleDetector;
	private boolean scalable;
	private final NumberFormat[] numberformatter = new NumberFormat[2];
	private final List<GraphViewSeries> graphSeries;
	private boolean showLegend = false;
	private float legendWidth = 120;
	private LegendAlign legendAlign = LegendAlign.MIDDLE;
	private boolean manualYAxis;
	private double manualMaxYValue;
	private double manualMinYValue;
	private GraphViewStyle graphViewStyle;
	private final GraphViewContentView graphViewContentView;
	private CustomLabelFormatter customLabelFormatter;
	private Integer labelTextHeight;
	private Integer horLabelTextWidth;
	private Integer verLabelTextWidth;
    private Integer verImagesWidth;
	private final Rect textBounds = new Rect();
	private boolean staticHorizontalLabels;
	private boolean staticVerticalLabels;
    private boolean allowRefresh = true;
    private Bitmap[] sideImages;

    public GraphView(Context context, AttributeSet attrs) {
        this(context, attrs, false);
    }

	public GraphView(Context context, AttributeSet attrs, boolean verticalLabelsOnRight) {
		this(context, attrs.getAttributeValue(null, "title"), verticalLabelsOnRight);

		int width = attrs.getAttributeIntValue("android", "layout_width", LayoutParams.MATCH_PARENT);
		int height = attrs.getAttributeIntValue("android", "layout_height", LayoutParams.MATCH_PARENT);
		setLayoutParams(new LayoutParams(width, height));
	}

	/**
	 * @param context
	 * @param title [optional]
	 */

    public GraphView(Context context, String title) {
        this(context, title, false);
    }

    public GraphView(Context context, String title, GraphViewStyle style, boolean verticalLabelsOnRight) {
        this(context, title, style, verticalLabelsOnRight, false);
    }

    public GraphView(Context context, String title, GraphViewStyle style, boolean verticalLabelsOnRight, boolean showSideImages) {

        super(context);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (title == null)
            this.title = "";
        else
            this.title = title;

        graphViewStyle = style;

        paint = new Paint();
        graphSeries = new ArrayList<GraphViewSeries>();

        viewVerLabels = new VerLabelsView(context, verticalLabelsOnRight);
        addView(viewVerLabels);

        if (showSideImages) {
            viewVerImages = new VerImagesView(context, !verticalLabelsOnRight);
            addView(viewVerImages);
        }

        graphViewContentView = new GraphViewContentView(context, verticalLabelsOnRight, showSideImages);
        addView(graphViewContentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

	public GraphView(Context context, String title, boolean verticalLabelsOnRight) {
		this(context, title, new GraphViewStyle(), verticalLabelsOnRight);
	}

	private GraphViewDataInterface[] _values(int idxSeries) {
		GraphViewDataInterface[] values = graphSeries.get(idxSeries).values;
		synchronized (values) {
			if (viewportStart == 0 && viewportSize == 0) {
				// all data
				return values;
			} else {
				// viewport
				List<GraphViewDataInterface> listData = new ArrayList<GraphViewDataInterface>();
				for (int i=0; i<values.length; i++) {
					if (values[i].getX() >= viewportStart) {
						if (values[i].getX() > viewportStart+viewportSize) {
							listData.add(values[i]); // one more for nice scrolling
							break;
						} else {
							listData.add(values[i]);
						}
					} else {
						if (listData.isEmpty()) {
							listData.add(values[i]);
						}
						listData.set(0, values[i]); // one before, for nice scrolling
					}
				}
				return listData.toArray(new GraphViewDataInterface[listData.size()]);
			}
		}
	}

	/**
	 * add a series of data to the graph
	 * @param series
	 */
	public void addSeries(GraphViewSeries series) {
		series.addGraphView(this);
		graphSeries.add(series);
		redrawAll();
	}

	protected void drawLegend(Canvas canvas, float height, float width) {
		int shapeSize = 15;

		// rect
		paint.setARGB(180, 100, 100, 100);
		float legendHeight = (shapeSize+5)*graphSeries.size() +5;
		float lLeft = width-legendWidth - 10;
		float lTop;
		switch (legendAlign) {
		case TOP:
			lTop = 10;
			break;
		case MIDDLE:
			lTop = height/2 - legendHeight/2;
			break;
		default:
			lTop = height - GraphViewConfig.BORDER - legendHeight -10;
		}
		float lRight = lLeft+legendWidth;
		float lBottom = lTop+legendHeight;
		canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, paint);

		for (int i=0; i<graphSeries.size(); i++) {
			paint.setColor(graphSeries.get(i).style.color);
			canvas.drawRect(new RectF(lLeft+5, lTop+5+(i*(shapeSize+5)), lLeft+5+shapeSize, lTop+((i+1)*(shapeSize+5))), paint);
			if (graphSeries.get(i).description != null) {
				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(graphSeries.get(i).description, lLeft+5+shapeSize+5, lTop+shapeSize+(i*(shapeSize+5)), paint);
			}
		}
	}

	abstract protected void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeriesStyle style, int[] colors);

	/**
	 * formats the label
	 * use #setCustomLabelFormatter or static labels if you want custom labels
	 * 
	 * @param value x and y values
	 * @param isValueX if false, value y wants to be formatted
	 * @deprecated use {@link #setCustomLabelFormatter(CustomLabelFormatter)}
	 * @return value to display
	 */
	@Deprecated
	protected String formatLabel(double value, boolean isValueX, int length, int index) {
		if (customLabelFormatter != null) {
			String label = customLabelFormatter.formatLabel(value, isValueX, length, index);
			if (label != null) {
				return label;
			}
		}
		int i = isValueX ? 1 : 0;
		if (numberformatter[i] == null) {
			numberformatter[i] = NumberFormat.getNumberInstance();
			double highestvalue = isValueX ? getMaxX(false) : getMaxY();
			double lowestvalue = isValueX ? getMinX(false) : getMinY();
			if (highestvalue - lowestvalue < 0.1) {
				numberformatter[i].setMaximumFractionDigits(6);
			} else if (highestvalue - lowestvalue < 1) {
				numberformatter[i].setMaximumFractionDigits(4);
			} else if (highestvalue - lowestvalue < 20) {
				numberformatter[i].setMaximumFractionDigits(3);
			} else if (highestvalue - lowestvalue < 100) {
				numberformatter[i].setMaximumFractionDigits(1);
			} else {
				numberformatter[i].setMaximumFractionDigits(0);
			}
		}
		return numberformatter[i].format(value);
	}

    protected String formatLabel(double value, boolean isValueX) {
        return formatLabel(value, isValueX, -1, -1);
    }

	private String[] generateHorlabels(float graphwidth) {
		int numLabels = getGraphViewStyle().getNumHorizontalLabels()-1;
		if (numLabels < 0) {
			numLabels = (int) (graphwidth/(horLabelTextWidth*2));
		}

		String[] labels = new String[numLabels+1];
		double min = getMinX(false);
		double max = getMaxX(false);
		for (int i=0; i<=numLabels; i++) {
			labels[i] = formatLabel(min + ((max-min)*i/numLabels), true, numLabels + 1, i);
		}
		return labels;
	}

	synchronized private String[] generateVerlabels(float graphheight) {
		int numLabels = getGraphViewStyle().getNumVerticalLabels()-1;
		if (numLabels < 0) {
			numLabels = (int) (graphheight/(labelTextHeight*3));
		}
		String[] labels = new String[numLabels+1];
		double min = getMinY();
		double max = getMaxY();
		if (max == min) {
			// if min/max is the same, fake it so that we can render a line
			if(max == 0) {
				// if both are zero, change the values to prevent division by zero
				max = 1.0d;
				min = 0.0d;
			} else {
				max = max*1.05d;
				min = min*0.95d;
			}
		}

		for (int i=0; i<=numLabels; i++) {
			labels[numLabels-i] = formatLabel(min + ((max-min)*i/numLabels), false, numLabels + 1, i);
		}
		return labels;
	}

    synchronized private Bitmap[] getVerImages() {
        int numLabels = getGraphViewStyle().getNumVerticalLabels();
        if (sideImages != null) {
            return Arrays.copyOf(sideImages, numLabels);
        }

        return new Bitmap[0];
    }

	/**
	 * @return the custom label formatter, if there is one. otherwise null
	 */
	public CustomLabelFormatter getCustomLabelFormatter() {
		return customLabelFormatter;
	}

	/**
	 * @return the graphview style. it will never be null.
	 */
	public GraphViewStyle getGraphViewStyle() {
		return graphViewStyle;
	}

	/**
	 * get the position of the legend
	 * @return
	 */
	public LegendAlign getLegendAlign() {
		return legendAlign;
	}

	/**
	 * @return legend width
	 */
	public float getLegendWidth() {
		return legendWidth;
	}

	/**
	 * returns the maximal X value of the current viewport (if viewport is set)
	 * otherwise maximal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxX(boolean ignoreViewport) {

		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart+viewportSize;
		} else {
			// otherwise use the max x value
			// values must be sorted by x, so the last value has the largest X value
			double highest = 0;
			if (graphSeries.size() > 0) {
				GraphViewDataInterface[] values = graphSeries.get(0).values;
				if (values.length == 0) {
					highest = 0;
				} else {
					highest = values[values.length-1].getX();
				}
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					if (values.length > 0) {
						highest = Math.max(highest, values[values.length-1].getX());
					}
				}
			}
			return highest;
		}
	}

	/**
	 * returns the maximal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMaxY() {
		double largest;
		if (manualYAxis) {
			largest = manualMaxYValue;
		} else {
			largest = Integer.MIN_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				GraphViewDataInterface[] values = _values(i);
				for (int ii=0; ii<values.length; ii++)
					if (values[ii].getY() > largest)
						largest = values[ii].getY();
			}
		}
		return largest;
	}

	/**
	 * returns the minimal X value of the current viewport (if viewport is set)
	 * otherwise minimal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart;
		} else {
			// otherwise use the min x value
			// values must be sorted by x, so the first value has the smallest X value
			double lowest = 0;
			if (graphSeries.size() > 0) {
				GraphViewDataInterface[] values = graphSeries.get(0).values;
				if (values.length == 0) {
					lowest = 0;
				} else {
					lowest = values[0].getX();
				}
				for (int i=1; i<graphSeries.size(); i++) {
					values = graphSeries.get(i).values;
					if (values.length > 0) {
						lowest = Math.min(lowest, values[0].getX());
					}
				}
			}
			return lowest;
		}
	}

    public double getViewPortStart() {
        return getMinX(false);
    }

	/**
	 * returns the minimal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected double getMinY() {
		double smallest;
		if (manualYAxis) {
			smallest = manualMinYValue;
		} else {
			smallest = Integer.MAX_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				GraphViewDataInterface[] values = _values(i);
				for (int ii=0; ii<values.length; ii++)
					if (values[ii].getY() < smallest)
						smallest = values[ii].getY();
			}
		}
		return smallest;
	}

	public boolean isDisableTouch() {
		return disableTouch;
	}

	public boolean isScrollable() {
		return scrollable;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	/**
	 * forces graphview to invalide all views and caches.
	 * Normally there is no need to call this manually.
	 */
	public void redrawAll() {
        if (allowRefresh) {
            if (!staticVerticalLabels) verlabels = null;
            if (!staticHorizontalLabels) horlabels = null;
            numberformatter[0] = null;
            numberformatter[1] = null;
            labelTextHeight = null;
            horLabelTextWidth = null;
            verLabelTextWidth = null;

            invalidate();
            viewVerLabels.invalidate();

            if (viewVerImages != null) {
                viewVerImages.invalidate();
            }

            graphViewContentView.invalidate();
        }
	}

	/**
	 * removes all series
	 */
	public void removeAllSeries() {
		for (GraphViewSeries s : graphSeries) {
			s.removeGraphView(this);
		}
		while (!graphSeries.isEmpty()) {
			graphSeries.remove(0);
		}
		redrawAll();
	}

	/**
	 * removes a series
	 * @param series series to remove
	 */
	public void removeSeries(GraphViewSeries series) {
		series.removeGraphView(this);
		graphSeries.remove(series);
		redrawAll();
	}

	/**
	 * removes series
	 * @param index 
	 */
	public void removeSeries(int index) {
		if (index < 0 || index >= graphSeries.size()) {
			throw new IndexOutOfBoundsException("No series at index " + index);
		}

		removeSeries(graphSeries.get(index));
	}

	/**
	 * scrolls to the last x-value
	 * @throws IllegalStateException if scrollable == false
	 */
	public void scrollToEnd() {
		if (!scrollable) throw new IllegalStateException("This GraphView is not scrollable.");

		double max = getMaxX(true);
		viewportStart = max-viewportSize;
		redrawAll();
	}

    /**
     * Used for smoother scrolling to the right
     */
    public void scrollToEndProperly() {
        if (!scrollable) throw new IllegalStateException("This GraphView is not scrollable.");

        double max = getMaxX(true);

        double diff = 0;
        if (max > ((viewportSize + viewportStart) - scrollPaddingRight)) {
            diff = max - ((viewportSize + viewportStart) - scrollPaddingRight);
        }

        viewportStart += diff;
        redrawAll();
    }

	/**
	 * set a custom label formatter
	 * @param customLabelFormatter
	 */
	public void setCustomLabelFormatter(CustomLabelFormatter customLabelFormatter) {
		this.customLabelFormatter = customLabelFormatter;
	}

	/**
	 * The user can disable any touch gestures, this is useful if you are using a real time graph, but don't want the user to interact
	 * @param disableTouch
	 */
	public void setDisableTouch(boolean disableTouch) {
		this.disableTouch = disableTouch;
	}

	/**
	 * set custom graphview style
	 * @param style
	 */
	public void setGraphViewStyle(GraphViewStyle style) {
		graphViewStyle = style;
		labelTextHeight = null;
	}

	/**
	 * set's static horizontal labels (from left to right)
	 * @param horlabels if null, labels were generated automatically
	 */
	public void setHorizontalLabels(String[] horlabels) {
		staticHorizontalLabels = horlabels != null;
		this.horlabels = horlabels;
	}

	/**
	 * legend position
	 * @param legendAlign
	 */
	public void setLegendAlign(LegendAlign legendAlign) {
		this.legendAlign = legendAlign;
	}

	/**
	 * legend width
	 * @param legendWidth
	 */
	public void setLegendWidth(float legendWidth) {
		this.legendWidth = legendWidth;
	}

	/**
	 * you have to set the bounds {@link #setManualYAxisBounds(double, double)}. That automatically enables manualYAxis-flag.
	 * if you want to disable the menual y axis, call this method with false.
	 * @param manualYAxis
	 */
	public void setManualYAxis(boolean manualYAxis) {
		this.manualYAxis = manualYAxis;
	}

	/**
	 * set manual Y axis limit
	 * @param max
	 * @param min
	 */
	public void setManualYAxisBounds(double max, double min) {
		manualMaxYValue = max;
		manualMinYValue = min;
		manualYAxis = true;
	}

	/**
	 * this forces scrollable = true
	 * @param scalable
	 */
	synchronized public void setScalable(boolean scalable) {
		this.scalable = scalable;
		if (scalable == true && scaleDetector == null) {
			scrollable = true; // automatically forces this
			scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
				@Override
				public boolean onScale(ScaleGestureDetector detector) {
					double center = viewportStart + viewportSize / 2;
					viewportSize /= detector.getScaleFactor();
					viewportStart = center - viewportSize / 2;

					// viewportStart must not be < minX
					double minX = getMinX(true);
					if (viewportStart < minX) {
						viewportStart = minX;
					}

					// viewportStart + viewportSize must not be > maxX
					double maxX = getMaxX(true);
					double overlap = viewportStart + viewportSize - maxX;
					if (overlap > 0) {
						// scroll left
						if (viewportStart-overlap > minX) {
							viewportStart -= overlap;
						} else {
							// maximal scale
							viewportStart = minX;
							viewportSize = maxX - viewportStart;
						}
					}
					redrawAll();
					return true;
				}
			});
		}
	}

	/**
	 * the user can scroll (horizontal) the graph. This is only useful if you use a viewport {@link #setViewPort(double, double)} which doesn't displays all data.
	 * @param scrollable
	 */
	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * sets the title of graphview
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

    /**
     * If we want to show vertical lines
     */
    public void setShowVerticalGridLines(boolean show) {
        this.showVerticalGridLines = show;
    }

	/**
	 * set's static vertical labels (from top to bottom)
	 * @param verlabels if null, labels were generated automatically
	 */
	public void setVerticalLabels(String[] verlabels) {
		staticVerticalLabels = verlabels != null;
		this.verlabels = verlabels;
	}

	/**
	 * set's the viewport for the graph.
	 * @see #setManualYAxisBounds(double, double) to limit the y-viewport
	 * @param start x-value
	 * @param size
	 */
    public void setViewPort(double start, double size) {
        setViewPort(start, size, 0, 0);
    }
	public void setViewPort(double start, double size, double paddingLeft, double paddingRight) {
        viewportStartInit = start;
		viewportStart = start;
		viewportSize = size;

        scrollPaddingLeft = paddingLeft;
        scrollPaddingRight = paddingRight;
	}

    public void setSideImages(Bitmap[] images) {
        sideImages = images;
    }

    public void setAllowRefresh(boolean allow) {
        allowRefresh = allow;
    }

    private void setViewLayoutParams(View view, int width, int height, int pxLeft, int pxRight) {
        final LayoutParams params = (LayoutParams)view.getLayoutParams();
        params.width = width;
        params.height = height;
        params.leftMargin = pxLeft;
        params.rightMargin = pxRight;
        view.setLayoutParams(params);
    }

    private void measureBottomText() {
        if (labelTextHeight == null) {
            paint.setTextSize(getGraphViewStyle().getTextSize());
            double testY = ((getMaxY()-getMinY())*0.783)+getMinY();
            String testLabel = formatLabel(testY, false);
            paint.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
            labelTextHeight = (textBounds.height());
        }
    }
}
