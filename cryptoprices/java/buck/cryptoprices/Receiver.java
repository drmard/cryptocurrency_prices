package buck.cryptoprices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;

public class Receiver extends BroadcastReceiver 
{  
    public static final String ACTION_ON_QUARTER_HOUR =
            "hsp.crypto.currency.rates.ON_QUARTER_HOUR";
    @Override public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        //Log.d("Receiver", "receiving Intent with action: "+ action);
        if (ACTION_ON_QUARTER_HOUR.equals(action))
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();
            setAlarm(context, ACTION_ON_QUARTER_HOUR);
            initProcessOfUpdatingData(context);
            wl.release();
 
        } else if (action.equals("android.intent.action.BOOT_COMPLETED"))
        {
            context.startService(new Intent(context, GetDataService.class));
        } else
        {
            return;
        }
    }

    private void initProcessOfUpdatingData(Context context) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int mn = c.get(Calendar.MINUTE);
        if ((hour == 4 || hour == 17 || hour == 10 || hour == 22)
                && mn == 15)
        {
            //Intent intent = new Intent(Constants.RECEIVE_QUARTER_HOUR_ALARM);
            context.sendBroadcast(new Intent(Constants.
                RECEIVE_QUARTER_HOUR_ALARM));
        }
    }

    private void setAlarm(Context context, String action) {
        Calendar nextQuarter = Calendar.getInstance();
        nextQuarter.set(Calendar.SECOND, 1);
        int minute = nextQuarter.get(Calendar.MINUTE);
        nextQuarter.add(Calendar.MINUTE, 15 - (minute % 15));
        long onQuarterHour = nextQuarter.getTimeInMillis();
        Intent i = new Intent(ACTION_ON_QUARTER_HOUR);

        PendingIntent quarterlyIntent = PendingIntent.getBroadcast(context, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = ((AlarmManager) context.
                getSystemService(Context.ALARM_SERVICE));
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 23) {
            am.setExact(AlarmManager.RTC_WAKEUP, onQuarterHour, quarterlyIntent);
        } else if (Build.VERSION.SDK_INT < 19){
            am.set(AlarmManager.RTC_WAKEUP, onQuarterHour, quarterlyIntent);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                onQuarterHour, quarterlyIntent);
        }
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Receiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.
            getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
