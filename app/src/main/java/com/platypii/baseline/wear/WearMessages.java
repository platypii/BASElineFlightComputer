package com.platypii.baseline.wear;

/**
 * Messages to be sent between wear and mobile device
 */
class WearMessages {

    static final String STATE_URI = "/baseline/app/state";

    static final String WEAR_PING = "/baseline/ping";

    static final String WEAR_APP_PREFIX = "/baseline/app";
    static final String WEAR_APP_INIT = WEAR_APP_PREFIX + "/init";
    static final String WEAR_APP_RECORD = WEAR_APP_PREFIX + "/logger/record";
    static final String WEAR_APP_STOP = WEAR_APP_PREFIX + "/logger/stop";
    static final String WEAR_APP_AUDIBLE_ENABLE = WEAR_APP_PREFIX + "/audible/enable";
    static final String WEAR_APP_AUDIBLE_DISABLE = WEAR_APP_PREFIX + "/audible/disable";

    private static final String WEAR_SERVICE_PREFIX = "/baseline/service";
    static final String WEAR_SERVICE_OPEN_APP = WEAR_SERVICE_PREFIX + "/openApp";
    static final String WEAR_SERVICE_PONG = WEAR_SERVICE_PREFIX + "/pong";

}
