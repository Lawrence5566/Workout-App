package uk.ac.lincoln.a16601608students.workoutapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import javax.net.ssl.HttpsURLConnection;

//from workshop code

public class HttpConnect {
    // where the returned json data from service will be stored when downloaded
    static String json = "";

    //your android activity will call this method and pass in the url of the REST service
    public String getJSONFromUrl(String url) {
        try {
            Log.e("JSON", "try connection ");

            URL u = new URL(url);
            HttpURLConnection restConnection = (HttpURLConnection) u.openConnection();
            restConnection.setRequestMethod("GET");
            restConnection.setRequestProperty("Content-length", "0");
            restConnection.setUseCaches(false);
            restConnection.setAllowUserInteraction(false);
            restConnection.setConnectTimeout(10000);
            restConnection.setReadTimeout(10000);

            restConnection.connect();
            int status = restConnection.getResponseCode();


            // switch statement to catch HTTP 200 and 201 errors
            switch (status) {
                case 200:
                case 201:
                    // live connection to your REST service is established here using getInputStream() method
                    BufferedReader br = new BufferedReader(new InputStreamReader(restConnection.getInputStream()));

                    // create a new string builder to store json data returned from the REST service
                    StringBuilder sb = new StringBuilder();
                    String line;

                    // loop through returned data line by line and append to stringbuilder 'sb' variable
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();

                    try {
                        json = sb.toString();
                    } catch (Exception e) {
                        Log.e("JSON", "Error parsing data " + e.toString());
                    }
                    // return JSON String containing data to Tweet activity (or whatever your activity is called!)
                    return json;
            }
            // HTTP 200 and 201 error handling from switch statement
        } catch (MalformedURLException ex) {
            Log.e("JSON", "Malformed URL ");
        } catch (IOException ex) {
            Log.e("JSON", "IO Exception ");
        }
        return null;
    }
}
