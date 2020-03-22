package org.mozilla.reference.browser.qwant;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class Analytics extends AsyncTask<String, Void, Boolean> {

    private final static String API_URL = "https://api.qwant.com/api/action/687561776569/";

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            String event = (params.length > 0) ? params[0] : "default_event";
            String appversion = (params.length > 1) ? params[1] : "0";

            URL url = new URL(API_URL + event);
            String data = getPostDataString(appversion);
            Log.d("QWANT_BROWSER", "tracking data: " + data);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(data);
            writer.flush();
            writer.close();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPostDataString(String appversion) {
        String huaweiid = getHuaweiKey("ro.HuaweiID.com.qwant.liberty");
        if (huaweiid == null || huaweiid.length() == 0) huaweiid = "Unknown";

        String huaweichannel = getHuaweiKey("ro.channel.com.qwant.liberty");
        if (huaweichannel == null || huaweichannel.length() == 0) huaweichannel = "Unknown";

        StringBuilder result = new StringBuilder();
        result.append("version=").append(appversion);
        result.append("&huawei_id=").append(huaweiid);
        result.append("&huawei_channel=").append(huaweichannel);
        return result.toString();
    }

    private String getHuaweiKey(String key) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            return (String) method.invoke(clazz, key);
        } catch (ClassNotFoundException e) {
            Log.e("QWANT_BROWSER", "get huawei key (" + key + ") meets ClassNotFoundException" + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e("QWANT_BROWSER", "get huawei key (" + key + ") meets NoSuchMethodException" + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("QWANT_BROWSER", "get huawei key (" + key + ") meets IllegalAccessException" + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e("QWANT_BROWSER", "get huawei key (" + key + ") meets InvocationTargetException" + e.getMessage());
        } catch (Exception e) {
            Log.e("QWANT_BROWSER", "get huawei key (" + key + ") meets Exception" + e.getMessage());
        }
        return null;
    }
}
