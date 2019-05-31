package uk.ac.lincoln.a16601608students.workoutapp;


import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//adapted from lincoln uni workshop task 5 @derekfoster
//also uses https://stackoverflow.com/questions/12252654/android-how-to-update-an-ui-from-asynctask-if-asynctask-is-in-a-separate-class

public class ParseLocJson extends AsyncTask<String, String, String>{
    private ReturnJson returnJson;

    JSONArray results;

    String apikey = "add api key here";

    //test coords: 53.232973799999996,-0.5458096000000001 (lat,long)

    // default url of the web service to call
    String yourServiceUrl;

    public ParseLocJson(ReturnJson activityContext, String loc){
        this.returnJson = activityContext;

        //set url with location
        yourServiceUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + loc +
                "&radius=5000" +
                "&types=gym" +
                "&key=" + apikey;
    }

    @Override
    // this method is used to connect to rest service and download data, in this case, and can be used to setup task (like show loading bar)
    protected void onPreExecute() {}

    @Override
    // this method runs background thread immediately after onPreExecute
    protected String doInBackground(String... arg0)  {
        //note: do not use weakActivity in here

        try {
            // create new instance of the httpConnect class
            HttpConnect jParser = new HttpConnect();

            // get json string from service url
            String json = jParser.getJSONFromUrl(yourServiceUrl);

            if (json == null){
                //something wrong with connection/no JSON returned, return null
                results = null;
                return null;
            }

            // parse returned json string into json object
            JSONObject jsonObject = new JSONObject(json);

            results = jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    // below method will run when service HTTP request is complete
    protected void onPostExecute(String str) {
        returnJson.ReturnJsonLocationInfo(results);

    }
}
