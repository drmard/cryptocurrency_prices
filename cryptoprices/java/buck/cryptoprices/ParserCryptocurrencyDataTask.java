package buck.cryptoprices;

import android.os.AsyncTask;
import android.os.Handler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
//import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
//import java.util.function.Supplier;

import hsp.parser.Parser;
import hsp.parser.Currency;

public class ParserCryptocurrencyDataTask extends
        AsyncTask<Void, Void, ArrayList<Currency>>
{
    private static final String TAG =
        ParserCryptocurrencyDataTask.class.getSimpleName();
    private Callback mCallback;
    private Handler mHandler;
    public static interface Callback {
        public void onComplete(ArrayList<Currency> rezult);
    }

    public ParserCryptocurrencyDataTask(String currency, int number,
            Handler handler, Callback callback)
    {
        suffix = currency;
        if (number != 20) {
            numberOfItems = number;
        }
        mHandler = handler;
        mCallback = callback;
    }
    private String suffix;
    private int numberOfItems = 20;

    @Override protected ArrayList<Currency> doInBackground(Void... params)
    { 
        ArrayList<Currency> aList = null;  
        /*
        CompletableFuture<ArrayList<Currency>> getd = CompletableFuture.supplyAsync(
          new Supplier<ArrayList<Currency>>() {
            @Override public ArrayList<Currency> get() {
              ArrayList<Currency> al = null;
              Parser p = new Parser(suffix, numberOfItems);
              try {
                  al = p.getData();
              } catch (MalformedURLException ex) {
              } catch (IOException ioe) {
              }
              return al;
            }
          }
        );  */
        try {
            Parser p = new Parser(suffix, numberOfItems);
            //arrayList = getd.get();
            aList = p.getData();
        } catch (Exception e) {
        }
        return aList;
    }

    @Override protected void
    onPostExecute(ArrayList<Currency> result)
    {
        if (mCallback != null) {
            mCallback.onComplete(result);
        } else {
        }
    }
}
