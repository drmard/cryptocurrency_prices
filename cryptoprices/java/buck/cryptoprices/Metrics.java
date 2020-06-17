package buck.cryptoprices;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

public class Metrics
{
  private Context mContext = null;
  public int dip;
  private int mWidthLand;
  public int[] param = new int[6];
  public Metrics(Context context, int width)
  {
    mWidthLand = width;
    if(context != null) {
      mContext = context;
    }
    dip = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
      (float)1, mContext.getResources().getDisplayMetrics());
    for (int j = 0; j < 6; j++) {
      param[j] = (int)(((float)mWidthLand / coeff[j]) / (float)dip);
    }
  }
  public double[] coeff = {
    854.0 / 38,
    854.0 / 160,
    854.0 / 172,
    854.0 / 135,
    854.0 / 168,
    854.0 / 184
  };
}
