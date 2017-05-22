package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "symbol";

    @BindView(R.id.tv_detail_header)
    TextView mStockDetailsHeader;

    @BindView(R.id.stock_chart)
    LineChart mStockChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        ButterKnife.bind(this);

        String symbol = getIntent().getStringExtra(EXTRA_SYMBOL);

        mStockDetailsHeader.setText(String.format("%s %s", symbol, getString(R.string.a11y_stock_details_header)));
        mStockChart.setAutoScaleMinMaxEnabled(true);

        getStockHistory(symbol);
    }

    private void getStockHistory(String symbol) {
        String history = getHistoryString(symbol);

        List<String[]> lines = getHistoryLines(history);

        ArrayList<Entry> entries = new ArrayList<>();

        final ArrayList<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;

        assert lines != null;
        for (int i = lines.size() - 1; i >= 0; i--)  {
            String[] line = lines.get(i);

            xAxisValues.add(Long.valueOf(line[0]));
            xAxisPosition++;

            Entry entry = new Entry(xAxisPosition, Float.valueOf(line[1]));
            entries.add(entry);
        }

        setupStockChart(symbol, entries, xAxisValues);
    }

    private void setupStockChart(String symbol, List<Entry> entries, final List<Long> xAxisValues) {
        LineDataSet lineDataSet = new LineDataSet(entries, symbol);
        lineDataSet.setLineWidth(1.75f);
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setCircleColor(Color.WHITE);
        lineDataSet.setHighLightColor(Color.WHITE);
        lineDataSet.setDrawValues(false);
        LineData lineData = new LineData(lineDataSet);

        mStockChart.setData(lineData);
        // no description text
        mStockChart.getDescription().setEnabled(false);

        // mChart.setDrawHorizontalGrid(false);
        //
        // enable / disable grid background
        mStockChart.setDrawGridBackground(false);
        mStockChart.setContentDescription(getString(R.string.a11y_stock_graph_label));

        // enable touch gestures
        mStockChart.setTouchEnabled(true);

        // enable scaling and dragging
        mStockChart.setDragEnabled(true);
        mStockChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mStockChart.setPinchZoom(false);

        XAxis xAxis = mStockChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get((int) value));
                return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date);
            }
        });
    }

    @Nullable
    private List<String[]> getHistoryLines(String history) {
        List<String[]> lines = null;
        CSVReader reader = new CSVReader(new StringReader((history)));

        try {
            lines = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lines;
    }

    private String getHistoryString(String symbol) {
        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol), null, null, null, null);

        String history = "";
        if (cursor != null && cursor.moveToFirst()) {
            history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            cursor.close();
        }
        return history;
    }

}
