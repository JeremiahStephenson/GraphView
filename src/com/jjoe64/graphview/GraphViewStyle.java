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

import android.graphics.Color;

/**
 * Styles for the GraphView
 * Important: Use GraphViewSeries.GraphViewSeriesStyle for series-specify styling
 *
 */
public class GraphViewStyle {
	private int verticalLabelsColor;
	private int horizontalLabelsColor;
	private int gridColor;
	private float textSize = 30f;
	private int verticalLabelsWidth;
    private int verticalImagesWidth;
	private int numVerticalLabels;
	private int numHorizontalLabels;
    private int verticalLabelsLeftMargin;
    private int verticalLabelsRightMargin;
    private int verticalImagesLeftMargin;
    private int verticalImagesRightMargin;
    private int[] lineGradientColors = null;
    private boolean showBottomLineAndLabels = true;

	public GraphViewStyle() {
		verticalLabelsColor = Color.WHITE;
		horizontalLabelsColor = Color.WHITE;
		gridColor = Color.DKGRAY;
	}

	public GraphViewStyle(int vLabelsColor, int hLabelsColor, int gridColor) {
		this.verticalLabelsColor = vLabelsColor;
		this.horizontalLabelsColor = hLabelsColor;
		this.gridColor = gridColor;
	}

	public int getGridColor() {
		return gridColor;
	}

	public int getHorizontalLabelsColor() {
		return horizontalLabelsColor;
	}

	public int getNumHorizontalLabels() {
		return numHorizontalLabels;
	}

	public int getNumVerticalLabels() {
		return numVerticalLabels;
	}

	public float getTextSize() {
		return textSize;
	}

	public int getVerticalLabelsColor() {
		return verticalLabelsColor;
	}

	public int getVerticalLabelsWidth() {
		return verticalLabelsWidth;
	}

    public int getVerticalImagesWidth() {
        return verticalImagesWidth;
    }

    public int getVerticalLabelsLeftMargin() {
        return verticalLabelsLeftMargin;
    }

    public int getVerticalLabelsRightMargin() {
        return verticalLabelsRightMargin;
    }

    public int getVerticalImagesLeftMargin() {
        return verticalImagesLeftMargin;
    }

    public int getVerticalImagesRightMargin() {
        return verticalImagesRightMargin;
    }

    public int[] getLineGradientColors() {
        return lineGradientColors;
    }

    public boolean getShowBottomLinesAndLabels() {
        return showBottomLineAndLabels;
    }

	public void setGridColor(int c) {
		gridColor = c;
	}

	public void setHorizontalLabelsColor(int c) {
		horizontalLabelsColor = c;
	}

	/**
	 * @param numHorizontalLabels 0 = auto
	 */
	public void setNumHorizontalLabels(int numHorizontalLabels) {
		this.numHorizontalLabels = numHorizontalLabels;
	}

	/**
	 * @param numVerticalLabels 0 = auto
	 */
	public void setNumVerticalLabels(int numVerticalLabels) {
		this.numVerticalLabels = numVerticalLabels;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public void setVerticalLabelsColor(int c) {
		verticalLabelsColor = c;
	}

	/**
	 * @param verticalLabelsWidth 0 = auto
	 */
	public void setVerticalLabelsWidth(int verticalLabelsWidth) {
		this.verticalLabelsWidth = verticalLabelsWidth;
	}

    public void setVerticalImagesWidth(int verticalImagesWidth) {
        this.verticalImagesWidth = verticalImagesWidth;
    }

    public void setVerticalLabelsMargins(int verticalLabelsLeftMargin, int verticalLabelsRightMargin) {
        this.verticalLabelsLeftMargin = verticalLabelsLeftMargin;
        this.verticalLabelsRightMargin = verticalLabelsRightMargin;
    }

    public void setVerticalImagesMargins(int verticalImagesLeftMargin, int verticalImagesRightMargin) {
        this.verticalImagesLeftMargin = verticalImagesLeftMargin;
        this.verticalImagesRightMargin = verticalImagesRightMargin;
    }

    public void setLineGradientColors(int[] colors) {
        this.lineGradientColors = colors;
    }

    /**
     * If we want to show the bottom line with labels
     */
    public void setShowBottomLineAndLabels(boolean show) {
        this.showBottomLineAndLabels = show;
    }
}
