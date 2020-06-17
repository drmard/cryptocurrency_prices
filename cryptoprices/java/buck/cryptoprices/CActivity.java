package buck.cryptoprices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import hsp.parser.Currency;

public class CActivity extends Activity
{
    private RelativeLayout rootLayout = null;
    private Context mContext;
    private static final String TAG = "CActivity";
    ImageView bCl, bSh;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = this;

        rootLayout =
          (RelativeLayout)findViewById(R.id.control_bars);
        //rootLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        bCl = (ImageView)findViewById(R.id.scb);
        bSh = (ImageView)findViewById(R.id.fcb);
    }

/****                        
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                getActivity().onBackPressed();
                break;
            case R.id.done:
                // Simple debounce - just ignore while checks are underway
                if (mProceedButtonPressed) {
                    return;
                }
                mProceedButtonPressed = true;
                onNext();
                break;
        }
    }*/
    public void onClick(View view)
    {
        int iD = view.getId();
        Log.d(TAG,"__ id == " + iD);
        if (iD == R.id.scb) {
          finish();
        } else if (iD == R.id.fcb) {
          final Intent intent = new Intent(this, Prices.class);
          startActivity(intent);
          finish();
        }
    }
}
