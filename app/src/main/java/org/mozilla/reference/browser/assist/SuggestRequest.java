package org.mozilla.reference.browser.assist;

import android.util.JsonReader;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

class SuggestRequest {
    private final static String LOGTAG = "QwantAssist";
    private final static String BASE_URL = "https://api.qwant.com/api/suggest/?client=opensearch&q=";

    private static InputStream getHttpStream(URL url) throws Exception {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return urlConnection.getInputStream();
            }
        } catch (Exception e) {
            throw new Exception("Request connection for suggest failed");
        }
        return null;
    }

    static ArrayList<SuggestItem> getSuggestions(String filter_string) {
        try {
            ArrayList<SuggestItem> result = new ArrayList<>();
            InputStream inputStream = getHttpStream(new URL(BASE_URL + filter_string + "&lang=" + Locale.getDefault().toString()));
            if (inputStream != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
                try {
                    reader.beginArray();
                    reader.skipValue();
                    reader.beginArray();
                    while (reader.hasNext()) {
                        result.add(new SuggestItem(SuggestItem.Type.QWANT_SUGGEST, reader.nextString()));
                    }
                    reader.endArray();
                    reader.endArray();
                } catch (Exception e) {
                    Log.e(LOGTAG, "error reading suggest result: " + e.getMessage());
                } finally {
                    reader.close();
                }
            }
            return result;
        } catch (Exception e) {
            Log.e(LOGTAG, "Impossible de rapatrier les données de suggest");
        }
        return new ArrayList<>();
    }
}
