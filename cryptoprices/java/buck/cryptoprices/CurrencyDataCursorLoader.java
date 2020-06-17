package buck.cryptoprices;

import android.content.Context;
import android.content.CursorLoader;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CurrencyDataCursorLoader extends CursorLoader
{
    private static final String TAG = CurrencyDataCursorLoader.class.
            getSimpleName();

    protected static final boolean DEBUG = false;
    private Context mContext = null;
    private android.database.Cursor cursor = null;
    private SQLiteDatabase mDBase = null;
    private String currency = null;

    public CurrencyDataCursorLoader(Context context, String cryptoCurrency) {
        super(context);
        mContext = context;
        currency = cryptoCurrency;
    }

    @Override public android.database.Cursor loadInBackground() {
        if (DEBUG) {
            Log.d (TAG, "loadInBackground()");
        }

        android.database.Cursor cursor = null;

        mDBase = mContext.openOrCreateDatabase(
                CryptoCurrencyDatabase.DATABASE_NAME,Context.MODE_PRIVATE,
                null);

        if (mDBase == null || currency == null) {
            return null;
        }
        String where = CryptoCurrencyDatabase.Columns.COLUMN_CRYPTO_CURRENCY_NAME +
            " IS NOT NULL AND " + 
            CryptoCurrencyDatabase.Columns.COLUMN_CRYPTO_CURRENCY_NAME +
            "  LIKE " + "'" + currency + "'";
        
        String sort = CryptoCurrencyDatabase.Columns.COLUMN_TIME + " DESC";

        try {
            cursor = mDBase.query(CryptoCurrencyDatabase.CRYPTOCURRENCY_TABLE,
                null, where, null, null, null, sort);
        } catch (SQLException e) {
            Log.e(TAG, "SQLException: \n" + e);  
        }

        return cursor; 
    }
}   

