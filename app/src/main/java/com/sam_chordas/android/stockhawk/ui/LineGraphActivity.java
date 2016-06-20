package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import java.util.ArrayList;

public class LineGraphActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 1;
    private Context mContext;
    private Cursor mCursor;
    private Intent mServiceIntent;
    private String mStockSymbol;
    private LineChartView mLineChartView;

    private String[] mLabels = {};
    private ArrayList<Float> mValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_line_graph);

        Intent intent = getIntent();
        mStockSymbol = intent.getStringExtra("itemSymbol");

        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null) {
            mServiceIntent.putExtra("tag", "linegraph");
            mServiceIntent.putExtra("symbol", mStockSymbol);
            startService(mServiceIntent);
        }

        mLineChartView = (LineChartView) findViewById(R.id.linechart);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(this,
                QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{mStockSymbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){

        mCursor = data;
        data.moveToFirst();
        int columnIndex = data.getColumnIndex("bid_price");
        while(data.moveToNext()) {
            mValues.add(Float.valueOf(data.getString(columnIndex)));
        }
        float[] values = new float[mValues.size()];
        int i = 0;

        for (Float f : mValues) {
            values[i++] = (f != null ? f : Float.NaN);
        }

        LineSet lineSet = new LineSet(mLabels, values);

        lineSet.setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.parseColor("#758cbb"))
                .setThickness(4)
                .setDashed(new float[]{10f, 10f});
        mLineChartView.addData(lineSet);

        mLineChartView.setBorderSpacing(Tools.fromDpToPx(15))
                .setAxisBorderValues(0, 20)
                .setYLabels(AxisController.LabelPosition.NONE)
                .setXAxis(false)
                .setYAxis(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
    }
}
