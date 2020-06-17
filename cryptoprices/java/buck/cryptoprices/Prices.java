package buck.cryptoprices;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.htmlcleaner.TagNode;
import hsp.parser.Currency;
import hsp.parser.Parser;

import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Prices extends Activity implements
    UIEventListener,ParserCryptocurrencyDataTask.Callback,
    LoaderCallbacks<ArrayList<Currency>>
{
    private Context mContext;
    private boolean tableAdded = false;
    private Metrics mtx = null;
    private static int mWidthLand = 0;
    private Intent intentStartService;
    private Currency[] mCurrency;
    private int items = 0;
    private Map<Integer, String> cMap = null;
    private View v0;
    private LayoutParams lp;
    private TableRow tbrow;
    private ArrayList<View> bView = null;
    private TextView tv;
    private TextView countdown = null, _t;
    private LayoutInflater li = null;
    private TableLayout tl, tl_head;
    private ArrayAdapter<String> s_adapter;
    private View [] views = null, popup_view;
    private Timer timer;
    private Spinner spinner;
    private ProgressBar mProgressBar;
    private LinearLayout lL;
    private CryptoCurrencyDataLoader mLoader;
    private ImageView bRefresh;
    private String mFiatCurrency = "USD";
    private PopupWindow popupWindow;
    private RelativeLayout fiat_USD, fiat_EUR;
    private TextView tFiat;

    private static final String TAG = Prices.class.getSimpleName();
    private static final int CRYPTO_DATA_LOADER_ID         = 0x101;
    static final int NUMBER_CURRENCY_NUMBER                     =0;
    static final int NUMBER_CURRENCY_NAME                       =1;
    static final int NUMBER_CURRENCY_FULLNAME                   =2;
    static final int NUMBER_PRICE_IN_CURRENT_FIAT_CURRENCY      =3;
    static final int NUMBER_CHANGE_PRICE_IN_LAST_12_HOUR        =4;
    static final int NUMBER_CHANGE_PRICE_IN_LAST_7_DAYS         =5;
    static final int NUMBER_PRICE_IN_BTC                        =6;
    static final int NUMBER_PRICE_IN_BTC_CH12H                  =7;
    static final int NUMBER_PRICE_IN_BTC_CH7D                   =8;

    static final int NUMBER_MARKET_CAPITALIZATION_IN_USD        =9;
    static final int NUMBER_MARKET_CAPITALIZATION               =10;
    static final int NUMBER_EXCHANGE_VOL                        =11; 
    static final int NUMBER_EXCHANGE_VOL_IN_BTC                 =12;
    static final int NUMBER_EXCHANGE_VOL_IN_CURRENT_CURRENCY    =13;

    @Override public void onEventUiUpdate(int event,Handler handler,Object obj)
    {
        switch (event) {
            case Constants.EVT_PROGRESS_UPDATE_START:
            case Constants.EVT_PROGRESS_UPDATE_FINISH:
                handler.sendEmptyMessage (event);
                break;
            default:
                break;
        }; 
    }

    public Handler msgHandler = new Handler()
    {
        public void handleMessage (Message m) {
            switch (m.what) {
                case Constants.MSG_FIAT_CURRENCY_CHANGE:
                    if (m.obj == null)
                        return;
                    tFiat.setText((String)m.obj);
                    Message mes = Message.obtain();
                    mes.what = Constants.MSG_START_UPDATE;
                    mes.obj = m.obj;
                    msgHandler.sendMessage(mes);  
                    break;

                case Constants.MSG_START_UPDATE:
                    if (mLoader != null)
                    {
                        if (m.obj != null) {
                            mLoader.setFiatCurrency((String)m.obj);
                            Log.d (TAG,"new fiat - "+(String)m.obj);
                            mLoader.forceLoad();
                        }
                    }
                    msgHandler.sendEmptyMessage (Constants.MSG_PROGRESS_UPDATE);
                    break;

                case Constants.MSG_PROGRESS_UPDATE:
                    lL.setVisibility (View.INVISIBLE);
                    mProgressBar.setVisibility (View.VISIBLE);
                    ((ViewGroup)tl).removeAllViews();
                    break;

                case Constants.EVT_PROGRESS_UPDATE_START:
                    break;

                case Constants.EVT_PROGRESS_UPDATE_FINISH:
                    break;

                case Constants.MSG_FINISH_LOAD:
                    mProgressBar.setVisibility (View.INVISIBLE);
                    lL.setVisibility (View.VISIBLE);
                    msgHandler.sendEmptyMessage (Constants.MSG_BUILD_TABLE);
                    break;

                case Constants.MSG_BUILD_TABLE:
                    if (tableAdded == false) {
                        buildTable(mContext, mtx);
                        tableAdded = true;
                    }
                    if (mCurrency != null)  {
                        fillTable(mContext, mCurrency, mtx);
                    }
                break;
		
                case Constants.MSG_START_SERVICE:
                    intentStartService =
                        new Intent(getBaseContext(),GetDataService.class);
                    startService(intentStartService);  
                break;
            }
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager =
          (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : 
                manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prices);
        mContext = this;
        li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        mProgressBar = (ProgressBar)findViewById (R.id.pbar);
        mProgressBar.setIndeterminate(true);
        lL = (LinearLayout) findViewById (R.id.dat);

        countdown = (TextView) findViewById(R.id.digclock);
        _t = (TextView) findViewById(R.id.cur_time);

        tl_head = (TableLayout)findViewById (R.id.tlayout_h);
        tl = (TableLayout)findViewById (R.id.tlayout);
        bRefresh = (ImageView)findViewById (R.id.refr);
        bView = new ArrayList<View>();

        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mWidthLand = metrics.widthPixels;
        mtx = new Metrics(mContext, mWidthLand);

        // Layout params for table's header
        lp = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 36, 1.0f);

        mLoader = (CryptoCurrencyDataLoader)getLoaderManager().initLoader(
            Constants.CRYPTO_DATA_LOADER_ID, null, this);
    }

    @Override public void onComplete (ArrayList<Currency> rezult) {
        if (rezult != null)
        {
            Message msg = Message.obtain();
            msg.what = Constants.MSG_DATA_OBTAINED;
            msg.obj = (Object)rezult;
            msgHandler.sendMessage(msg);
        }
    }

    @Override
    public Loader<ArrayList<Currency>> onCreateLoader (int id, Bundle args) {
        CryptoCurrencyDataLoader loader = null;

        if (id == Constants.CRYPTO_DATA_LOADER_ID)
        {
            loader = new CryptoCurrencyDataLoader(this,
                msgHandler, mFiatCurrency);
        }

        return loader;
    }

    @Override public void onLoadFinished(
            Loader<ArrayList<Currency>> loader,
            ArrayList<Currency> data)
    {
        int loaderId = loader.getId();
        if (loaderId == Constants.CRYPTO_DATA_LOADER_ID && data != null &&
            data.size() > 0)
        {
            mCurrency = new Currency[data.size()];
            data.toArray(mCurrency);
            items = data.size();
        }
        msgHandler.sendEmptyMessageDelayed (Constants.MSG_FINISH_LOAD, 10); 
    }

    @Override public void onLoaderReset(
        Loader<ArrayList<Currency>> loader) {
    }

    View.OnClickListener pListener = new View.OnClickListener() {
        @Override public void onClick (View v) {
            if (popupWindow != null)
                popupWindow.dismiss();

            int iD = v.getId();
            if (iD == R.id.item_usd)
                mFiatCurrency = "USD";
            else if (iD == R.id.item_eur)
                mFiatCurrency = "EUR";
            String cur = "";
            if (tFiat != null) {
                cur = tFiat.getText().toString(); 
            }

            if (mFiatCurrency.equals(new String(cur)) == false) {
                Message m = Message.obtain();
                m.what = Constants.MSG_FIAT_CURRENCY_CHANGE;
                m.obj = (Object)mFiatCurrency;
                msgHandler.sendMessage(m);
            }      
        }
    };

    public void refreshCryptoData(View v, String fiatC)
    {
        if (fiatC == null)
            return;
        Message message = Message.obtain();
        message.what = Constants.MSG_START_UPDATE;
        message.obj = (Object)fiatC;
        msgHandler.sendMessage(message);
    }

    View.OnClickListener mListener = new View.OnClickListener()
    {
        @Override public void onClick(View view)
        {
            int iD = view.getId();
            startSelected (iD, cMap, mContext);
        }
    };

    public void updateViews (Object currency) {
    }

    private String _time () {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        return String.format("%02d:%02d", h, m);        
    }

    private void startSelected(int id, Map<Integer,String> c, Context context) {
        if (c != null && id > 0) {
            String currency = c.get(id);
            if (currency != null) {
                Intent i = new Intent(context, ASelected.class);
                i.putExtra(Constants.EXTRA_CRYPTOCURRENCY_NAME,currency);
                startActivity(i);
            }
        }
    }

    final Runnable updateTask = new Runnable() {
      public void run(){
        _t.setText(_time());
        countdown.setText(getCurrentTimeString());
      }
    };

    @Override public void onResume() {
        super.onResume();
        if (!isMyServiceRunning(GetDataService.class)){
            msgHandler.sendEmptyMessageDelayed (Constants.MSG_START_SERVICE, 50);
        }
        _t.setText(_time());
        countdown.setText(getCurrentTimeString());
        timer = new Timer("DC");
        Calendar calendar = Calendar.getInstance();
        int msec = 999 - calendar.get(Calendar.MILLISECOND);
        timer.scheduleAtFixedRate(
            new TimerTask()
            {
              @Override public void run()
              {
                runOnUiThread(updateTask);
              }
            }, msec, 1100
        );
    }

    private int chkFirst (String s) {
        if (s.charAt(0) == '-') {
            return 1;
        } else if (s.charAt(0) == '+') {
            return 2;
        } else {
            return 0;
        }
    }

    private String getCurrentTimeString()
    {
        Calendar c = Calendar.getInstance();
        int y = c.get(java.util.Calendar.YEAR);
        int mo = c.get(java.util.Calendar.MONTH);
        int d = c.get(java.util.Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        String day_w = c.getDisplayName(Calendar.DAY_OF_WEEK,
            Calendar.LONG, Locale.getDefault());
        String my = getMonth(c);
        return String.format("%s, %s %d, %d", day_w, my, d, y);
    }

    private String getMonth (Calendar calendar) {
        int m = calendar.get(Calendar.MONTH);
        String sm = "";
        switch (m) {
          case 0:
              sm = "January";
              break;
          case 1:
              sm = "February";
              break;
          case 2:
              sm = "March";
              break;

          case 3:
              sm = "April";
              break;

          case 4:
              sm = "May";
              break;

          case 5:
              sm = "June";
              break;

          case 6:
              sm = "July";
              break;

          case 7:
              sm = "August";
              break;

          case 8:
              sm = "September";
              break;

          case 9:
              sm = "October";
              break;

          case 10:
              sm = "November";
              break;

          case 11:
              sm = "December";
              break;

          default:
              sm = "";
              break;
        } 
        return sm;
    }

    private void showPopupMenu(View v, Context context)
    {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(R.menu.popupmenu);
        popupMenu.setOnMenuItemClickListener(
            new PopupMenu.OnMenuItemClickListener()
        {
            @Override public boolean onMenuItemClick(MenuItem item)
            {
                int id = item.getItemId();
                if (id == R.id.menu_usd)
                    mFiatCurrency = new String("USD");
                else if (id == R.id.menu_eur)
                    mFiatCurrency = new String("EUR");
                else if (id == R.id.menu_rub)
                    mFiatCurrency = new String("RUB");
                else
                    return false;

                refreshCryptoData(null, mFiatCurrency);
                return true;
            }
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override public void onDismiss(PopupMenu menu) {
                //
            }
        });

        popupMenu.show();
    }

    public View createView(int id, Currency curr, LayoutInflater li,
        int resource, int width, int bkgResource)
    {
        View v = li.inflate (resource, null);
        LinearLayout ll = (LinearLayout)v.findViewById (R.id.imgCurr);
        String nameRes = curr.getData(NUMBER_CURRENCY_NAME).toLowerCase();
        int resId = getResources().getIdentifier(nameRes, "drawable", mContext.getPackageName());
        if (ll != null && resId > 0) {
            ll.setBackground (getResources().getDrawable(resId));
        }
        else {
            ll.setBackground (getResources().getDrawable(R.drawable.cry));
        }
        TextView tv1 = (TextView)v.findViewById (R.id.index0);
        tv1.setText(curr.getData(NUMBER_CURRENCY_NAME));
        TextView tv2 =  (TextView)v.findViewById (R.id.title);
        tv2.setText (curr.getData(NUMBER_CURRENCY_FULLNAME));

        TableRow.LayoutParams _lp =
            new TableRow.LayoutParams(width, LayoutParams.MATCH_PARENT);
        v.setLayoutParams(_lp);
        v.setClickable(true);
        v.setOnClickListener(mListener);
        v.setId(id);
        v.setBackgroundResource(bkgResource);
        return v;
    }

    private void setTextViewColor(TextView tv)
    {
      if (tv == null)
        return;
      String s = tv.getText().toString(); 
      tv.setTextColor(s.charAt(0) == '-' ?
          android.graphics.Color.parseColor("#662222") :
              android.graphics.Color.parseColor("#226622"));
    }

    public void buildTable(Context context, Metrics mtx) {
        int k = 0, w, j = 1;
        tbrow = new TableRow(context);
        
        v0 = li.inflate (R.layout.cell_text, null);
        tv = (TextView)v0.findViewById(R.id.t1);
        tv.setText("# ");
        v0.setId (10 * j);
        v0.setBackgroundResource(R.layout.shape8);
        w = ((int)(mtx.param[k] * mtx.dip));
        lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
        v0.setLayoutParams( lp );
        tbrow.addView(v0);

        j++;
        k++;
        v0 = li.inflate (R.layout.cell_text, null);
        if (v0 != null) {
            tv = (TextView)v0.findViewById(R.id.t1);
        }
        tv.setText("Cryptocurrency");
        v0.setId (10 * j);
        v0.setBackgroundResource (R.layout.shape8);
        w = ((int)(mtx.param[k] * mtx.dip));
        lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
        v0.setLayoutParams(lp);
        int height = v0.getLayoutParams().height;
        int width  = v0.getLayoutParams().width;
        tbrow.addView( v0 );

        j++;
        k++;
        final View vg = li.inflate(R.layout.price_in, null);
        if (vg != null)
        {
                View fiatt = (View) vg.findViewById(R.id.explist);
                tFiat = (TextView)fiatt.findViewById(R.id.fiat_c);

                fiatt.setOnClickListener(new View.OnClickListener()
                {
                    @Override public void onClick(View v)
                    {
                        setPopUpWindow();
                        popupWindow.showAsDropDown(v, 1, 4);
                    }
                });

                v0.setId (10 * j);
                vg.setBackgroundResource (R.layout.shape8);
                w = ((int)(mtx.param[k] * mtx.dip));
                lp = new TableRow.LayoutParams(w, LayoutParams.WRAP_CONTENT);
                vg.setLayoutParams( lp );
                tbrow.addView(vg);
        }

        j++;
        k++;
        v0 = li.inflate (R.layout.cell_text, null);
        if (v0 != null)
            tv = (TextView)v0.findViewById(R.id.t1);
        tv.setText(" Price in BTC ");
        v0.setId (10 * j);
        v0.setBackgroundResource (R.layout.shape8);
        w = ((int)(mtx.param[k] * mtx.dip));
        lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
        v0.setLayoutParams(lp);
        tbrow.addView( v0 );

        j++; 
        k++;
        v0 = li.inflate (R.layout.cell_text, null);
        tv = (TextView)v0.findViewById(R.id.t1);
        tv.setText(" Market Capitalization ");
        v0.setId (10 * j);
        v0.setBackgroundResource (R.layout.shape8);
        w = ((int)(mtx.param[k] * mtx.dip));
        lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
        v0.setLayoutParams(lp);
        tbrow.addView( v0 );

        j++;
        k++;
        v0 = li.inflate (R.layout.cell_text, null);
        tv = (TextView)v0.findViewById(R.id.t1);
        tv.setText(" Exchange volume 24h ");
        v0.setId (10 * j);
        v0.setBackgroundResource (R.layout.shape8);
        w = ((int)(mtx.param[k] * mtx.dip));
        lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
        v0.setLayoutParams(lp);
        tbrow.addView( v0 );
        tl_head.addView (tbrow, new TableLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void ClickPopup(View view)
    {
        int iD = view.getId();
        if (iD == R.id.item_usd)
          Log.d(TAG, "USD ; iD - "+ "R.id.currency_usd");
        else if (iD == R.id.item_eur)
          Log.d(TAG, "EUR ; iD - "+ "R.id.currency_eur");  
    }

    private void setPopUpWindow() {
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View _view = inflater.inflate(R.layout.popup, null);
        fiat_USD = (RelativeLayout) _view.findViewById(R.id.item_usd);
        fiat_USD.setOnClickListener (pListener);
        fiat_EUR = (RelativeLayout) _view.findViewById(R.id.item_eur);
        fiat_EUR.setOnClickListener (pListener);
        
        popupWindow = new PopupWindow(_view, 260,
            RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    public View crView(int id, Currency curr, LayoutInflater li,
        int resource, int width, int bkgResource) {
        View v = li.inflate (resource, null);
        LinearLayout ll = (LinearLayout)v.findViewById (R.id.imgCurr);
        String nameRes = curr.getData(NUMBER_CURRENCY_NAME).toLowerCase();
        int resId =
          getResources().getIdentifier(nameRes,"drawable",mContext.getPackageName());
        if (ll != null && resId > 0) {
            ll.setBackground (getResources().getDrawable(resId));
        }
        else {
            ll.setBackground (getResources().getDrawable(R.drawable.cry));
        }
        TextView tv1 = (TextView)v.findViewById (R.id.index0);
        tv1.setText(curr.getData(NUMBER_CURRENCY_NAME));
        TextView tv2 =  (TextView)v.findViewById (R.id.title);
        tv2.setText (curr.getData(NUMBER_CURRENCY_FULLNAME));

        TableRow.LayoutParams _lp =
            new TableRow.LayoutParams(width, LayoutParams.MATCH_PARENT);
        v.setLayoutParams(_lp);
        v.setClickable(true);
        v.setOnClickListener(mListener);
        v.setId(id);
        v.setBackgroundResource(bkgResource);
        return v;
    }

    public void fillTable(Context context, Currency[] cur, Metrics mtx)
    {
        int sel = 0;
        int curId = -1;
        views = null;
        views = new View[20];
        
        cMap = new HashMap<Integer, String>();

        int j = 0, i, w;

        while (true)
        {
          if (j > 19) {
              break;
          }
          i = 0;
          tbrow = new TableRow(context);

          // 0
          v0 = li.inflate (R.layout.cell_text, null);
          if (v0 != null) {
              tv = (TextView)v0.findViewById(R.id.t1);
          }
          tv.setText(cur[j].getData(NUMBER_CURRENCY_NUMBER));
          curId = 10 * (j + 1) + i;
          v0.setId (curId);

          v0.setBackgroundResource(R.layout.shape8);
          w = ((int)(mtx.param[i] * mtx.dip));
          lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
          v0.setLayoutParams(lp);
          tbrow.addView(v0);

          i++; // 1
          int _id = 10 * (j + 1) + i;
          String name = cur[j].getData(NUMBER_CURRENCY_NAME);
          String full_name = cur[j].getData(NUMBER_CURRENCY_FULLNAME);
          int _w = ((int)(mtx.param[i] * mtx.dip));
          views[sel] = crView(_id, cur[j], li, R.layout.cell_coin, _w, R.layout.shape8);
          tbrow.addView(views[sel]);
          cMap.put (_id, new String(name));
          sel++;


        i++; // 2
        v0 = li.inflate (R.layout.price_in_usd, null);
        if (v0 != null)
        {
            TextView tv1 = (TextView)v0.findViewById (R.id.price);
            tv1.setText (cur[j].getData(NUMBER_PRICE_IN_CURRENT_FIAT_CURRENCY));
            TextView tv2 = (TextView)v0.findViewById (R.id.ch12h);
            tv2.setText (cur[j].getData(NUMBER_CHANGE_PRICE_IN_LAST_12_HOUR));
            setTextViewColor(tv2);

            TextView tv3 = (TextView)v0.findViewById (R.id.ch7d);
            tv3.setText (cur[j].getData(NUMBER_CHANGE_PRICE_IN_LAST_7_DAYS)); 
            setTextViewColor(tv3);
         
            v0.setId(10 * (j + 1) + i);
            v0.setBackgroundResource (R.layout.shape8);
            w = ((int)(mtx.param[i] * mtx.dip));
            lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
            v0.setLayoutParams( lp );
            tbrow.addView( v0 );
        }
													
        i++;//3
        v0 = li.inflate(R.layout.price_in_btc, null);
        if (v0 != null) {
            TextView tv1 = (TextView)v0.findViewById (R.id.price_in_btc);
            tv1.setText(cur[j].getData(NUMBER_PRICE_IN_BTC));
            TextView tv2 = (TextView)v0.findViewById (R.id.price_in_btc_ch12h);
            tv2.setText(cur[j].getData(NUMBER_PRICE_IN_BTC_CH12H));
            setTextViewColor(tv2);

            TextView tv3 = (TextView)v0.findViewById(R.id.price_in_btc_ch7d) ;
            tv3.setText(cur[j].getData(NUMBER_PRICE_IN_BTC_CH7D));
            setTextViewColor(tv3);

            v0.setId(10 * (j + 1) + i);
            v0.setBackgroundResource(R.layout.shape8);
            w = ((int)(mtx.param[i] * mtx.dip));
            lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
            v0.setLayoutParams( lp );
            tbrow.addView( v0 );
        }

        i++;  //4
        v0 = li.inflate(R.layout.market_cap, null);
        if ( v0 != null ){
            TextView tv1 = (TextView)v0.findViewById (R.id.market_kap_in_usd);
            tv1.setText(cur[j].getData(NUMBER_MARKET_CAPITALIZATION_IN_USD));
            TextView tv2 = (TextView)v0.findViewById (R.id.market_kap);
            tv2.setText(cur[j].getData(NUMBER_MARKET_CAPITALIZATION));
            v0.setId(10 * (j + 1) + i);
            v0.setBackgroundResource(R.layout.shape8);
            w = ((int)(mtx.param[i] * mtx.dip));
            lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
            v0.setLayoutParams( lp );
            tbrow.addView( v0 );            
        }

        i++; //5
        v0 = li.inflate(R.layout.exchange_volume_24h, null);
        if (v0 != null) {
            TextView tv1 = (TextView)v0.findViewById (R.id.exchange_vol);
            tv1.setText(cur[j].getData(NUMBER_EXCHANGE_VOL));
            TextView tv2 = (TextView)v0.findViewById (R.id.exchange_vol_in_btc);
            tv2.setText(cur[j].getData(NUMBER_EXCHANGE_VOL_IN_BTC));
            TextView tv3 =
              (TextView)v0.findViewById(R.id.exchange_vol_in_current_currency);
            tv3.setText(cur[j].getData(NUMBER_EXCHANGE_VOL_IN_CURRENT_CURRENCY));

            v0.setId(10 * (j + 1) + i);
            v0.setBackgroundResource(R.layout.shape8);
            w = ((int)(mtx.param[i] * mtx.dip));
            lp = new TableRow.LayoutParams(w, LayoutParams.MATCH_PARENT);
            v0.setLayoutParams( lp );
            tbrow.addView( v0 );
        }
        tl.addView (tbrow, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT));      

        j++;
        }     
    }

    public class CSpinner extends Spinner
    {
        private final int mIndex;
        private CSpinner cSpinner;
        public CSpinner (int index)
        {
            super(Prices.this);
            mIndex = index;
            cSpinner = this;
            setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { 
                @Override
                public void onItemSelected(AdapterView<?> parentView,
                        View selectedItem, int position, long id)
                {
                    String curr = cSpinner.getSelectedItem().toString();
                    if (curr != null) 
                        updateViews( (Object)curr );
                }
                @Override
                public void onNothingSelected(AdapterView<?> parentView)
                {
                }
            });
        }
    }

    @Override public void onBackPressed() {
        if(popupWindow != null && popupWindow.isShowing())
        {
            popupWindow.dismiss();
            return;
        } else {
            super.onBackPressed();
        }
    }
}
