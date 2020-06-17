package buck.cryptoprices;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import android.os.SystemClock;
import android.provider.BaseColumns;
import android.text.format.Time;
import android.util.Log;

import hsp.parser.Currency;

import java.util.Calendar;

public class CryptoCurrencyDatabase
{
    private static final String TAG = CryptoCurrencyDatabase.class.getSimpleName();

    public CryptoCurrencyDatabase (Context context) {
        mContext = context;
    }

    private static Time t;
    static final String DATABASE_NAME = "cryptoprices.db";
    static final String CRYPTOCURRENCY_TABLE = "crypto_prices";
    static final int DB_VERSION = 1;

    private Context mContext;
    public SQLiteDatabase mDBase;
    public SQLiteDatabase getDB () {
        return mDBase;
    }

    static final class Columns implements BaseColumns {
        private Columns() {
        }
        public static final String COLUMN_ID                   = "_id"; 
        public static final String COLUMN_TIME                 = "time";
        // USD, EURO ...
        public static final String COLUMN_FIAT_CURRENCY        = "fiat_curr";
        public static final String COLUMN_CRYPTO_CURRENCY_NAME = "crypto_name";
        public static final String COLUMN_PRICE_IN_USD         = "price"; 
        public static final String COLUMN_INCR_12H             = "incr_12h";
        public static final String COLUMN_INCR_7D              = "incr_7d";
        public static final String COLUMN_PRICE_BTC            = "price_btc";
        public static final String COLUMN_PRICE_BTC_CH_12H     = "price_btc_12h";
        public static final String COLUMN_PRICE_BTC_CH_7D      = "price_btc_7d";
        public static final String COLUMN_MARKET_CAP_IN_USD    = "market_cap_usd";
        public static final String COLUMN_MARKET_CAP           = "market_cap";
        public static final String COLUMN_EXCH24               = "exch24";
        public static final String COLUMN_EXCH24_IN_BTC        = "exch24_btc";
        public static final String COLUMN_EXCH24_IN_USD        = "exch24_usd";          
    }

    static final int COLUMN_ID_N                     = 0;
    static final int COLUMN_TIME_N                   = 1;
    static final int COLUMN_FIAT_CURRENCY_N          = 2;
    static final int COLUMN_CRYPTO_CURRENCY_NAME_N   = 3;
    static final int COLUMN_PRICE_IN_CURRENT_N       = 4;
    static final int COLUMN_INCR_12H_N               = 5;
    static final int COLUMN_INCR_7D_N                = 6;
    static final int COLUMN_PRICE_BTC_N              = 7;
    static final int COLUMN_PRICE_BTC_CH_12H_N       = 8;
    static final int COLUMN_PRICE_BTC_CH_7D_N        = 9;
    static final int COLUMN_MARKET_CAP_IN_USD_N      = 10;
    static final int COLUMN_MARKET_CAP_N             = 11;
    static final int COLUMN_EXCH24_N                 = 12;
    static final int COLUMN_EXCH24_IN_BTC_N          = 13;

    private static final String DATABASE_TABLE_CREATE =
        "create table "
        + CRYPTOCURRENCY_TABLE
        + " ("
        + Columns.COLUMN_ID + " integer primary key autoincrement, "
        + Columns.COLUMN_TIME + " text, "
        + Columns.COLUMN_FIAT_CURRENCY + " text, " +
        Columns.COLUMN_CRYPTO_CURRENCY_NAME + " text, " +
        Columns.COLUMN_PRICE_IN_USD + " text, " +
        Columns.COLUMN_INCR_12H + " text, " +
        Columns.COLUMN_INCR_7D + " text, " +
        Columns.COLUMN_PRICE_BTC + " text, " +
        Columns.COLUMN_PRICE_BTC_CH_12H + " text, " +
        Columns.COLUMN_PRICE_BTC_CH_7D + " text, " +
        Columns.COLUMN_MARKET_CAP_IN_USD + " text, " +
        Columns.COLUMN_MARKET_CAP + " text, " +
        Columns.COLUMN_EXCH24 + " text, " +
        Columns.COLUMN_EXCH24_IN_BTC + " text, " +
        Columns.COLUMN_EXCH24_IN_USD + " text" +
        ");";

    public void openDB() {
        DatabaseHelper helper =
            new DatabaseHelper(mContext, DATABASE_NAME, null, DB_VERSION);
        mDBase = helper.getWritableDatabase();
        if (mDBase != null)
            Log.d (TAG, "created db " + DATABASE_NAME);
        else Log.d(TAG,"DB not created ...");
    }

    private static String c_time () {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        int y = c.get(Calendar.YEAR);
        int mon = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        return String.format("%d/%02d/%02d %02d:%02d", y, mon + 1, d, h, m);        
    }

    public static void addDataToDb (Currency c, SQLiteDatabase dB) {
        Log.d ("DBase", "addDataToDb");
        ContentValues cv = new ContentValues();

        cv.put(Columns.COLUMN_FIAT_CURRENCY, c.currentCurrency);
        cv.put(Columns.COLUMN_CRYPTO_CURRENCY_NAME, c.name);
        cv.put(Columns.COLUMN_PRICE_IN_USD, c.priceInCurrent);
        cv.put(Columns.COLUMN_INCR_12H, c.incr_12h);
        cv.put(Columns.COLUMN_INCR_7D, c.incr_7d);
        cv.put(Columns.COLUMN_PRICE_BTC, c.price_BTC);
        cv.put(Columns.COLUMN_PRICE_BTC_CH_12H, c.price_BTC_12h);
        cv.put(Columns.COLUMN_PRICE_BTC_CH_7D, c.price_BTC_7d);
        cv.put(Columns.COLUMN_MARKET_CAP_IN_USD, c.marketCap_in_USD);
        cv.put(Columns.COLUMN_MARKET_CAP, c.marketCap_is);
        cv.put(Columns.COLUMN_EXCH24, c.exch24);
        cv.put(Columns.COLUMN_EXCH24_IN_BTC, c.exch24_in_BTC);
        cv.put(Columns.COLUMN_EXCH24_IN_USD, c.exch24_in_USD);

        String date_time = c_time();
        cv.put(Columns.COLUMN_TIME, date_time);

        dB.insert(CRYPTOCURRENCY_TABLE, null, cv);
    }

    static class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper (Context context, String name,
                CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_TABLE_CREATE);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion,
            int newVersion) {
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                int newVersion) {
        }
    }
}


