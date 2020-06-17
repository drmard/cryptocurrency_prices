//package hsp.crypto.currency.rates;
package buck.cryptoprices;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import hsp.parser.Parser;
import hsp.parser.Currency;

public class GetDataService extends Service implements
        ParserCryptocurrencyDataTask.Callback
{
    private static final String TAG = GetDataService.class.getSimpleName();

    private static final boolean DEBUG = false;
    static final int BLOCKING_QUEUE_SIZE = 2;

    private Context mContext = null;
    private NotificationManager mNotificationManager = null;
    private Receiver receiver = null;
    private boolean nativeInitComplete = false;
    private ServiceHandler mHandler = null;

    private String currentCurrency;
    private CryptoCurrencyDatabase mDbase = null;
    private SQLiteDatabase dB = null;

    @Override public void onComplete (ArrayList<Currency> rezult) {
        Log.d (TAG,"onComplete ...\n");
        if (rezult != null) {
            Log.d(TAG, "rezult: number - " + rezult.size());
                Message msg = Message.obtain();
                msg.what = Constants.MSG_SAVE_DATA;
                msg.obj = (Object)rezult;
                mHandler.sendMessage(msg);
        } else {
            Log.d(TAG, "rezult == null .......");
        }
    }

    private void saveCryptocurrencyData(ArrayList<Currency> al) {
        Log.d(TAG, "saveCryptocurrencyData start ...");
        int i, numb = 0;
        numb = al.size();
        dB = mContext.openOrCreateDatabase (
            CryptoCurrencyDatabase.DATABASE_NAME, Context.MODE_PRIVATE, null);
        if (dB == null) {
            Log.d(TAG,"dB is null ... Data not saved");
            return;
        }
        for (i = 0; i < numb; i++) {
            Currency currency = al.get(i);
            CryptoCurrencyDatabase.addDataToDb(currency, dB);
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override 
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        receiver = new Receiver();
        mNotificationManager = (NotificationManager)getSystemService(
            NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        mNotificationManager.cancel(Constants.CRYPTO_NOTIFICATION_ID);
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("Test", "Service: onTaskRemoved");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        int returnValue = super.onStartCommand(intent, flags, startId);

        try {
            mContext = getBaseContext();
        } catch (SecurityException e) {
            String errorLog = "onStartCommand:" + e.toString();
            Log.e(TAG, errorLog);
            throw new SecurityException(errorLog);
        }

        if (mContext != null){
            mNotificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        mDbase = new CryptoCurrencyDatabase(mContext);
        if(mDbase != null)
        Log.d(TAG, "#### created CryptoCurrencyDatabase - " + mDbase);



        mDbase.openDB();

        initNative();
        final IntentFilter filter = new IntentFilter ();
        filter.addAction(Constants.RECEIVE_QUARTER_HOUR_ALARM);
        registerReceiver(mReceiver, filter);
        
        setAlarmOnQuarterHour(mContext);
        startNotification(mContext);
        return START_STICKY;

    }

    private void initNative ()
    {
        Log.d(TAG, "initNative() starts ...") ;

        if (mHandler == null)
            mHandler = new ServiceHandler(mContext);

        if (nativeInitComplete == false) {
            Message msg = Message.obtain();
            msg.what = Constants.MSG_INIT_NATIVE;
            mHandler.sendMessage(msg);

            nativeInitComplete = true;   // ????
        }
    }

    private void setAlarmOnQuarterHour(Context context) {
        Calendar nextQuarter = Calendar.getInstance();
        // set 1 second to ensure quarter-hour threshold passed.
        nextQuarter.set(Calendar.SECOND, 1);
        int minute = nextQuarter.get(Calendar.MINUTE);
        nextQuarter.add(Calendar.MINUTE, 15 - (minute % 15));
        long onQuarterHour = nextQuarter.getTimeInMillis();

        PendingIntent quarterlyIntent = PendingIntent.getBroadcast(
            context, 0, new Intent(Receiver.ACTION_ON_QUARTER_HOUR),
            PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE));

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, onQuarterHour, quarterlyIntent);
        } else if (Build.VERSION.SDK_INT < 19){
            alarmManager.set(AlarmManager.RTC_WAKEUP, onQuarterHour, quarterlyIntent);
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                onQuarterHour, quarterlyIntent);
        }
        Log.d(TAG,"alarm on Quarter HOUR was set ...");     
    }

    //
    public void startNotification(Context context)
    {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.notif_layout);

        views.setImageViewResource(R.id.ic_not, R.drawable.ic_launcher);
        views.setTextViewText(R.id.crypto_not, " Crypto Prices");
        Intent notificationIntent = new Intent(this, /*CryptoPrices*/Prices.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification status = new Notification();
        status.contentView = views;
        status.flags |= Notification.FLAG_ONGOING_EVENT;

        status.icon = R.drawable.ic_launcher;
        status.contentIntent = contentIntent;
        startForeground(Constants.CRYPTO_NOTIFICATION_ID, status);
    }
    
    private void SendMessage(int msgWhat, Object obj, Handler handler) {
        Message msg = Message.obtain();
        msg.what = msgWhat;
        if (obj != null) {
            msg.obj = obj;
        }
        handler.sendMessage(msg);
    }

    public void setFiatCurrency (String curr) {
        currentCurrency = curr;
    }

    private void initLoading() {
        if (currentCurrency == null) {
            setFiatCurrency("USD");
        }   
        ParserCryptocurrencyDataTask task =
            new ParserCryptocurrencyDataTask(currentCurrency,
            20, mHandler, this);
        if (task != null) {
            task.execute();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Constants.RECEIVE_QUARTER_HOUR_ALARM))
            {
                SendMessage(Constants.MSG_GET_CRYPTOCURRENCY_DATA, null, mHandler);
            }
        }
    };

    private class ServiceHandler extends Handler {
        private Context mContext;

        public ServiceHandler (Context context) {
            mContext = context;
        }

        public ServiceHandler (Context context, Looper looper) {
            super(looper);
            mContext = context;
        }

	@Override
	public void handleMessage (Message msg) {
            switch (msg.what) {
                case Constants.MSG_UPDATE_CRYPTOCURRENCY_DATA:
                    //int build_version = android.os.Build.VERSION.SDK_INT;
                break;

                case Constants.MSG_GET_CRYPTOCURRENCY_DATA:
                    Log.d(TAG, "handle message - " + Constants.MSG_GET_CRYPTOCURRENCY_DATA);
                    initLoading();
                break;

                case Constants.MSG_SAVE_DATA:
                    if (msg.obj != null) {
                        saveCryptocurrencyData((ArrayList<Currency>)msg.obj);
                    }
                break;
            }
        }
    }
}
