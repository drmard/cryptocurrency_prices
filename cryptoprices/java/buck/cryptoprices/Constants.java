package buck.cryptoprices;

public class Constants
{
    public static final int MSG_START_UPDATE         = 20003;
    public static final int MSG_DATA_OBTAINED        = 20004;
    public static final int MSG_START_SERVICE        = 20005;

    public static final int MSG_PROGRESS_UPDATE      = 20006;
    public static final int MSG_INIT_NATIVE          = 20007;
    public static final int MSG_FINISH_LOAD          = 20008;

    public static final int CRYPTO_CURSOR_LOADER_ID         = 1;

    public static final int MSG_GET_CRYPTOCURRENCY_DATA     = 30001;
    public static final int MSG_UPDATE_CRYPTOCURRENCY_DATA  = 30002;
    public static final int MSG_SAVE_DATA                   = 30003;
    public static final int MSG_BUILD_TABLE                 = 30004;
    public static final int MSG_FIAT_CURRENCY_CHANGE        = 30007;   

    public static final int MSG_SHOW_SELECTED               = 20040;

    public static final int CRYPTO_NOTIFICATION_ID          = 0x04;
    public static final int CRYPTO_DATA_LOADER_ID           = 0x01;
    public static final int NUMBER_OF_ITEMS                 = 20;

    public static final String RECEIVE_QUARTER_HOUR_ALARM   =
        "buck.cryptoprices.RECEIVE_QUARTER_HOUR_ALARM";

    public static final int EVT_PROGRESS_UPDATE_START     =   0x612;
    public static final int EVT_PROGRESS_UPDATE_FINISH    =   0x613;

    public static final String EXTRA_CRYPTOCURRENCY_NAME  =   "crypto_name";
}

