package buck.cryptoprices;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

public class MetricsII
{
  private Context mContext = null;
  public int dip;
  private int mWidthLand;
  public int[] param = new int[4];
  public MetricsII(Context context, int width)
  {
    mWidthLand = width;
    if(context != null)
    {
      mContext = context;
    }
    dip = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
      (float)1, mContext.getResources().getDisplayMetrics());
    for (int j = 0; j < 4; j++)
    {
      param[j]= (int)(((float)mWidthLand / coeff[j]) / (float)dip);
      //Log.d ("__", "param"+ j +" = " + param[j]);
    }
  }

  public double[] coeff =
  {
    854.0 / 178,
    854.0 / 134,
    854.0 / 162,
    854.0 / 188
  };

}
