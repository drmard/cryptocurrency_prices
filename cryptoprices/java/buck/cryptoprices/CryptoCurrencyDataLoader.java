package buck.cryptoprices;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import hsp.parser.Currency;
import hsp.parser.Parser;

public class CryptoCurrencyDataLoader extends
        AsyncTaskLoader<ArrayList<Currency>> 
{
    private static final String TAG = CryptoCurrencyDataLoader.class.
        getSimpleName();
    private Handler mHandler;
    private String urlString, currentFiatCurrency = null;
    private URL url;
    private Parser parser;

    public void setHandler (Handler handler)
    {
        if (handler != null)
            mHandler = handler;
    }

    public Handler getHandler () {
        return mHandler;
    }

    public CryptoCurrencyDataLoader(
        Context context, Handler handler, String currency) {
        super(context);
        parser = null;
        if (currency != null) {
            Log.d (TAG, "CryptoCurrencyDataLoader - constructor: currency - "+currency);
        }

        mHandler = handler;
        currentFiatCurrency = new String(currency);
        parser = new Parser(currentFiatCurrency, Constants.NUMBER_OF_ITEMS);
    }

    public void setFiatCurrency(String selectedFiatCurrency) {
        currentFiatCurrency = new String(selectedFiatCurrency);
        Log.d ("setFiatCurrency","curr - " +currentFiatCurrency);
        parser = new Parser(currentFiatCurrency, Constants.NUMBER_OF_ITEMS);
        if (parser != null)
        {
            Log.d("PATH","parser path - "+ parser.getPath()); 
        }
    }

    @Override
    public void onStartLoading() {
            super.onStartLoading();
            Log.d(TAG, hashCode() + "- onStartLoading");
            forceLoad();
    }

    @Override
    public void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
    }

    @Override
    protected void onReset() {
        stopLoading();
    }

    @Override
    public void onForceLoad() {
        super.onForceLoad();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(Constants.MSG_PROGRESS_UPDATE);
        }
    }

    @Override
    public void deliverResult(ArrayList<Currency> result) {
            Log.d (TAG,"deliverResult() starts ...");
            if (result == null || isReset()) {
                Log.d (TAG,"result == NULL or isReset() ...");
                return;
            }
            super.deliverResult(result);
    }

    @Override public ArrayList<Currency> loadInBackground() {
        ArrayList<Currency> alist = null;
        if (parser == null)
        parser =
            new Parser(currentFiatCurrency, Constants.NUMBER_OF_ITEMS);
        if (parser == null) {
          return alist;
        }
        try {
            alist = parser.getData();
        } catch (Exception ex) {
            Log.d ("ERROR", "" + ex);
        }
        return alist;
    }
}

