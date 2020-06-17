package buck.cryptoprices;

import android.os.Handler;

public interface UIEventListener {
    abstract void onEventUiUpdate(int event, Handler handler, Object obj);
}
