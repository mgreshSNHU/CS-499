package com.example.impetus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Custom View that renders the user's weight history as a line chart.
 */
public class WeightChartView extends View {

    // Chart drawing tools
    private final Paint linePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint pointPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint gridPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    // Weight values displayed by the chart
    private final List<Double> weights =
            new ArrayList<>();

    public WeightChartView(Context context) {
        super(context);
        initialize();
    }

    public WeightChartView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        super(context, attrs);
        initialize();
    }

    public WeightChartView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    /**
     * Configures paint objects used to build chart elements.
     */
    private void initialize() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setColor(
                getResources().getColor(
                        com.google.android.material.R.color.material_dynamic_primary50,
                        null
                )
        );

        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(
                linePaint.getColor()
        );

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setColor(
                getResources().getColor(
                        android.R.color.darker_gray,
                        null
                )
        );
        gridPaint.setAlpha(60);

        textPaint.setTextSize(28f);
        textPaint.setColor(
                getResources().getColor(
                        android.R.color.darker_gray,
                        null
                )
        );
    }

    /**
     * Replaces the current chart data and redraws the view.
     */
    public void setWeights(List<Double> newWeights) {
        weights.clear();
        weights.addAll(newWeights);

        invalidate();
    }

    /**
     * Draws the chart using the supplied weight values.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (weights.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        float leftPadding = 90f;
        float rightPadding = 30f;
        float topPadding = 40f;
        float bottomPadding = 60f;

        float chartWidth =
                getWidth() - leftPadding - rightPadding;

        float chartHeight =
                getHeight() - topPadding - bottomPadding;

        double minWeight =
                Collections.min(weights);

        double maxWeight =
                Collections.max(weights);

        // Prevent division by zero when every recorded weight is identical.
        if (maxWeight == minWeight) {
            maxWeight += 1;
            minWeight -= 1;
        }

        double range =
                maxWeight - minWeight;

        // Add vertical breathing room so points do not touch chart edges.
        minWeight -= range * 0.10;
        maxWeight += range * 0.10;

        range =
                maxWeight - minWeight;

        drawGrid(
                canvas,
                leftPadding,
                topPadding,
                chartWidth,
                chartHeight,
                minWeight,
                maxWeight
        );

        drawWeightLine(
                canvas,
                leftPadding,
                topPadding,
                chartWidth,
                chartHeight,
                minWeight,
                range
        );
    }

    /**
     * Draws the connected weight line and individual data points.
     */
    private void drawWeightLine(
            Canvas canvas,
            float leftPadding,
            float topPadding,
            float chartWidth,
            float chartHeight,
            double minWeight,
            double range
    ) {
        Path path = new Path();

        for (int i = 0; i < weights.size(); i++) {
            float x;

            if (weights.size() == 1) {
                x = leftPadding
                        + chartWidth / 2f;
            } else {
                x = leftPadding
                        + (
                        chartWidth
                                * i
                                / (weights.size() - 1)
                );
            }

            // Convert the weight value into a vertical chart position.
            double normalized =
                    (weights.get(i) - minWeight)
                            / range;

            float y =
                    topPadding
                            + chartHeight
                            - (float) (
                            normalized * chartHeight
                    );

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }

            canvas.drawCircle(
                    x,
                    y,
                    8f,
                    pointPaint
            );
        }

        canvas.drawPath(
                path,
                linePaint
        );
    }

    /**
     * Draws horizontal grid lines and their corresponding weight labels.
     */
    private void drawGrid(
            Canvas canvas,
            float left,
            float top,
            float width,
            float height,
            double minWeight,
            double maxWeight
    ) {
        int lines = 4;

        for (int i = 0; i <= lines; i++) {
            float y =
                    top + (height * i / lines);

            canvas.drawLine(
                    left,
                    y,
                    left + width,
                    y,
                    gridPaint
            );

            double value =
                    maxWeight
                            - (
                            (maxWeight - minWeight)
                                    * i
                                    / lines
                    );

            canvas.drawText(
                    String.format(
                            Locale.getDefault(),
                            "%.1f",
                            value
                    ),
                    8f,
                    y + 10f,
                    textPaint
            );
        }
    }

    /**
     * Displays guidance when the user has not recorded any weigh-ins.
     */
    private void drawEmptyState(Canvas canvas) {
        String message =
                getResources().getString(
                        R.string.chart_empty_state
                );

        float textWidth =
                textPaint.measureText(message);

        canvas.drawText(
                message,
                (getWidth() - textWidth) / 2f,
                getHeight() / 2f,
                textPaint
        );
    }
}