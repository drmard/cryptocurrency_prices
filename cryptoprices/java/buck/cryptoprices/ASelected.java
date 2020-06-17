package buck.cryptoprices;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Calendar;

public class ASelected  extends Activity implements
        LoaderCallbacks<Cursor>
{
    private Context mContext;
    private String mCryptoCurrency = null;
    private java.util.ArrayList<CItem> mCurrencyItems;
    private TextView mC = null;
    private CurrencyDataCursorLoader mLoader = null;
    private android.widget.ArrayAdapter mAdapter;
    private android.widget.ListView mCurrencyList = null;
    private TableLayout tableLayout,tableLayoutH;
    private LayoutInflater layoutI;
    private static int width;
    private MetricsII mtx;
    private LayoutParams lParams;
    private CryptoCurrencyDatabase dB;
    private boolean backPressed = false;

    String[] crArray = {"Date/Time", "Price", "Price in BTC", "Market Capitalization"};

    private void build_cr_table() {
    }
    private View makeCell(int index, int sec_index, String text) {
        View v = layoutI.inflate(R.layout.cr_text0, null);
        TextView textView = (TextView)v.findViewById(R.id.txt);
        textView.setText(text);
        v.setId (10 * sec_index + index);
                        v.setBackgroundResource(R.layout.shape6);
                        int width = ((int)(mtx.param[index] * mtx.dip));
                        TableRow.LayoutParams params = new TableRow.LayoutParams(
                                width, LayoutParams.MATCH_PARENT);
        v.setLayoutParams(params);
        return v;
    }

    public class CItem {
        public String time;
        public String name;
        public String price;
        public String price_BTC;
        public String marketCap;
    }

    public Handler mHandler = new Handler() {
        private CItem it;
        public void handleMessage (Message msg) {
            switch (msg.what) {
                case Constants.MSG_SHOW_SELECTED:
                    if (msg.obj == null) {
                        break;
                    }
                    android.database.Cursor c = (android.database.Cursor)msg.obj;
                    if (c == null) {
                        break;
                    }
                    int len = c.getCount();
                    Log.d ("records", "records - " + len);
                    if (len == 0) {
                        break;
                    }
                    c.moveToPosition(-1);
                    
                    mCurrencyItems = null;
                    mCurrencyItems = new java.util.ArrayList<CItem>();
                    while (c.moveToNext())
                    {
                        it = new CItem();

                        String dat = c.getString(CryptoCurrencyDatabase.COLUMN_TIME_N);
                        if (dat != null)
                        it.time = new String(dat);
                        
                        String pr = c.getString(CryptoCurrencyDatabase.
                            COLUMN_PRICE_IN_CURRENT_N);
                        if (pr != null)
                        it.price = new String(pr);

                        String tb = c.getString(CryptoCurrencyDatabase.
                            COLUMN_PRICE_BTC_N);
                        if (tb != null)
                        it.price_BTC = new String(tb);

                        String markCap = c.getString(CryptoCurrencyDatabase.
                            COLUMN_MARKET_CAP_N);
                        if (markCap != null)
                        it.marketCap = new String(markCap);

                        mCurrencyItems.add(it);
                    }

                    int sZ = mCurrencyItems.size();
                    c.close();
                    ((ViewGroup)tableLayout).removeAllViews();
                    TableRow tbrow;
                    View v0; 
                    TextView textView;
                    int curId = -1, i = 0, j = 1, w, k = 0;
                    tbrow = new TableRow(mContext);

                    // build header
                    View view;
                    while (i < crArray.length) {
                        view = makeCell(i, j, crArray[i]);
                        tbrow.addView(view);
                        i++;
                    }
                    tableLayoutH.addView (tbrow, new TableLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
                    // build header end

                    j++;
                    while (true) {
                        if (k == sZ || k == 128) {
                            break;
                        }

                        CItem ci = mCurrencyItems.get(k);
                        tbrow = new TableRow(mContext);

                        //0
                        i = 0;
                        v0 = layoutI.inflate (R.layout.cr_text, null);
                        textView = (TextView)v0.findViewById(R.id.txt);
                        textView.setText(ci.time);

                        curId = 10 * j + i;
                        v0.setId (curId);
                        v0.setBackgroundResource(R.layout.shape6);
                        w = ((int)(mtx.param[i] * mtx.dip));
                        lParams = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
                        v0.setLayoutParams(lParams);
                        tbrow.addView(v0);

                        //1
                        i++;
                        v0 = null;
                        v0 = layoutI.inflate (R.layout.cr_text, null);
                        textView = (TextView)v0.findViewById(R.id.txt);
                        textView.setText(ci.price);
                        curId = 10 * j + i;
                        v0.setId (curId);
                        v0.setBackgroundResource(R.layout.shape6);
                        w = ((int)(mtx.param[i] * mtx.dip));
                        lParams = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
                        v0.setLayoutParams(lParams);
                        tbrow.addView(v0);

                        //2
                        i++;
                        v0 = null;
                        v0 = layoutI.inflate (R.layout.cr_text, null);
                        textView = (TextView)v0.findViewById(R.id.txt);
                        textView.setText(ci.price_BTC);
                        curId = 10 * j + i;
                        v0.setId (curId);
                        v0.setBackgroundResource(R.layout.shape6);
                        w = ((int)(mtx.param[i] * mtx.dip));
                        lParams = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
                        v0.setLayoutParams(lParams);
                        tbrow.addView(v0);

                        //3
                        i++;
                        v0 = null;
                        v0 = layoutI.inflate (R.layout.cr_text, null);
                        textView = (TextView)v0.findViewById(R.id.txt);
                        textView.setText(ci.marketCap);
                        curId = 10 * j + i;
                        v0.setId (curId);
                        v0.setBackgroundResource(R.layout.shape6);
                        w = ((int)(mtx.param[i] * mtx.dip));
                        lParams = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
                        v0.setLayoutParams(lParams);
                        tbrow.addView(v0);

                        tableLayout.addView (tbrow, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT)); 

                        j++;
                        k++;
                    }
                    break;
            }
        }
    };

    private String c_time () {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        int y = c.get(Calendar.YEAR);
        int mon = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        return String.format("%d/%02d/%02d %02d:%02d", y, mon, d, h, m);        
    }

    public void Click_s(View v) {
        finish();
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (Context)this;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select);
        mC = (TextView)findViewById(R.id.sel_currency);
        String strSelected = null;
        Intent intent = getIntent();
        strSelected = intent.getStringExtra(Constants.EXTRA_CRYPTOCURRENCY_NAME);
        if (strSelected != null) {
            mC.setText(strSelected);
            mCryptoCurrency = strSelected;
        } 
        mCurrencyItems = new java.util.ArrayList<CItem>();
        layoutI = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

        tableLayout = (TableLayout)findViewById (R.id.cur_layout);
        tableLayoutH = (TableLayout)findViewById (R.id.h_layout);


        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        mtx = new MetricsII(mContext, width);
        dB = new CryptoCurrencyDatabase (this);

    }

    @Override public void onResume() {
        Log.d(" "," start");
        super.onResume();
        mLoader = (CurrencyDataCursorLoader)getLoaderManager().initLoader(
            Constants.CRYPTO_CURSOR_LOADER_ID, null, this);
        Log.d("ASelected", "" + c_time());
    }

    @Override public void onBackPressed() {
        backPressed = true;
    }	

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Log.d ("ASelected", "onCreateLoader");
        return new CurrencyDataCursorLoader (mContext,
            mCryptoCurrency == null ? "BTC" : mCryptoCurrency);
    }

    /*****
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CellDataCursorLoader(ctx, null);
    }            */

    /*     
    private void equalizerSpinnerInit(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,android.R.layout.simple_spinner_item, mEQPresetNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != mEQPresetPrevious) {
                    equalizerSetPreset(position);
                }
                mEQPresetPrevious = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(mEQPreset);
    }*/

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d("ASelected", "onLoadFinished");
        CurrencyDataCursorLoader mLoader = null;
        int iD = loader.getId();
        switch (iD)
        {
            case Constants.CRYPTO_CURSOR_LOADER_ID:
                if (cursor != null) {
                    Log.d ("onLoadFinished", "cursor - " + cursor);
                    android.os.Message message = Message.obtain();
                    message.what = Constants.MSG_SHOW_SELECTED;
                    message.obj = (Object)cursor;
                    mHandler.sendMessage(message);
                }
                break;
            default:
                break;
        }
    }

    @Override public void onLoaderReset(Loader<Cursor> loader)
    {
        Log.d("ASelected", "onLoaderReset");
    }

}
