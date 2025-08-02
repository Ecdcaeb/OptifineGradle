package io.github.ecdcaeb.gradle.optifine.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class NetworkUtil {
    public static String redirectUrl(String url) throws IOException{
        URLConnection urlConnection = new URL(url).openConnection();
        if (urlConnection instanceof HttpURLConnection httpURLConnection) {
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.connect();
            if (String.valueOf(httpURLConnection.getResponseCode()).startsWith("3")) {
                String newUrl = httpURLConnection.getHeaderField("Location");
                if (newUrl != null) return redirectUrl(newUrl);
            }
        }
        return url;
    }
}
