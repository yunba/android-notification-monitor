package io.yunba.notificationmonitor;

/**
 * Created by miao on 2017/2/24.
 */

public interface ILogger {
    static int primary = 1;
    static int success = 2;
    static int waring = 3;
    static int danger = 4;
    static int info = 5;

    void onLogMessage(String msg);
    void onLogMessage(String msg, int logLevel);
}
