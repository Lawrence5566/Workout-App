package uk.ac.lincoln.a16601608students.workoutapp;


import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//adapted from lincoln uni workshop task 5 @derekfoster
//also uses https://stackoverflow.com/questions/12252654/android-how-to-update-an-ui-from-asynctask-if-asynctask-is-in-a-separate-class

public class ParseJson extends AsyncTask<String, String, String>{
    private ReturnJson returnJson;

    // set the url of the web service to call
    String yourServiceUrl = "https://wger.de/api/v2/";
    String currentExtension;
    JSONArray results;

    //pass in context as constructor from SelectExercise
    public ParseJson(ReturnJson activityContext, String extension){
        this.returnJson = activityContext;
        currentExtension = extension;
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
            String json = jParser.getJSONFromUrl(yourServiceUrl + currentExtension);

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
        if (currentExtension == "exercisecategory/"){
            returnJson.ReturnJson(results);
        } else {
            returnJson.ReturnJsonExercises(results);
        }

    }
}