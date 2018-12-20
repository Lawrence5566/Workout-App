package uk.ac.lincoln.a16601608students.workoutapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


//using from https://stackoverflow.com/questions/10407159/how-to-manage-startactivityforresult-on-android/10407371#10407371

//this activity calls ParseJson and displays categories and exercises for the user to select
//it also saves the API data in local storage and retrieves it instead of calling the api
//      if we have it (only makes calls to API when needed, only stores when needed)
public class SelectExercise extends AppCompatActivity implements ReturnJson {
    int Id;             //id of TextView in table that this exercise selection was opened for
    private String LastCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectexercise);

        Intent intent = getIntent();
        Id = intent.getIntExtra("ID", 1);

        //before accessing api for categories, check if you already have the data:
        SharedPreferences ExerciseCategories = getSharedPreferences("ExerciseAPI", Context.MODE_PRIVATE);
        if (!ExerciseCategories.contains("Categories")){
            //if we have no categories saved, retrieve some:
            parseJson("exercisecategory/");
        } else{ //we already have categories, find them from this phone
            String JsonText = ExerciseCategories.getString("Categories", "");
            try{
                JSONArray jsonArray = new JSONArray(JsonText);
                ReturnJson(jsonArray);
            }catch (JSONException e){
                Toast.makeText(this, "parse failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void parseJson(String extension){
        new ParseJson(this, extension).execute();
    }

    public void returnText(String text){ //send data back to table;
        Intent returnIntent = new Intent();
        returnIntent.putExtra("resultText", text);
        returnIntent.putExtra("resultID", Id);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    //for returning categories
    @Override
    public void ReturnJson(JSONArray result) { //set up list of categories
        Log.d("JSON", "returned to select exercise");
        //check if parsejson returned null
        if (result == null){
            returnText("no data");
            Toast.makeText(this, "please connect to the internet and try again", Toast.LENGTH_LONG).show();
        } else{

            // save it in shared preferences for later!
            try {
                SharedPreferences ExerciseCategories = getSharedPreferences("ExerciseAPI", Context.MODE_PRIVATE);
                if (!ExerciseCategories.contains("Categories")) { //if its not already there
                    SharedPreferences.Editor editor = ExerciseCategories.edit();
                    editor.putString("Categories", result.toString());
                    editor.commit();
                }

                // show toast message for successful save
            }
            catch(Exception ex) {
                // error
                Toast.makeText(this, "Error: Categories not saved", Toast.LENGTH_LONG).show();
            }

            ListView listView = findViewById(R.id.apiListView);
            ArrayList<String> categories = new ArrayList<String>();
            ArrayList<Integer> Ids = new ArrayList<Integer>();

            try {
                // loop through json array "results"
                for (int i = 0; i < result.length(); i++) {
                    JSONObject json_obj = result.getJSONObject(i);
                    if (json_obj != null) {
                        //extract "name" and "id" of exercise category from object
                        categories.add(json_obj.getString("name"));
                        Ids.add(json_obj.getInt("id") );
                    }
                }
            } catch (JSONException e) { //using getJSONObject so need catch
                e.printStackTrace();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, categories);
            listView.setAdapter(adapter);//create list

            listView.setClickable(true);
            final String[] categoryNames = categories.toArray(new String[categories.size()]);
            final Integer[] ids = Ids.toArray(new Integer[Ids.size()]);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                    checkForExercises(categoryNames[position], "exercise/?muscles=" + ids[position]);
                }
            });
        }
    }

    public void checkForExercises(String lastCategory, String extension){
        LastCategory = lastCategory;
        //before accessing api for Exercises, check if you already have the data:
        SharedPreferences ExerciseCategories = getSharedPreferences("ExerciseAPI", Context.MODE_PRIVATE);
        if (!ExerciseCategories.contains("Categories-" +lastCategory)){
            //if we have no Exercises of this category saved
            parseJson(extension);
        } else{ //we already have these Exercises, find them from this phone
            String JsonText = ExerciseCategories.getString("Categories-" + lastCategory, "");
            try{
                JSONArray jsonArray = new JSONArray(JsonText);
                ReturnJsonExercises(jsonArray);
            }catch (JSONException e){
                Toast.makeText(this, "parse failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    //for returning a filtered list of exercises
    @Override
    public void ReturnJsonExercises(JSONArray result){
        ArrayList<String> exercises = new ArrayList<String>();
        ListView listView = findViewById(R.id.apiListView);

        if (result == null){
            Toast.makeText(this, "Connect to the internet", Toast.LENGTH_LONG).show();
        }

        try {
            // loop through json array "results"
            for (int i = 0; i < result.length(); i++) {
                JSONObject json_obj = result.getJSONObject(i);
                if (json_obj != null) {
                    //extract "name" of exercise from object
                    exercises.add(json_obj.getString("name"));
                }
            }

            // save exercises in shared preferences for later!
            SharedPreferences ExerciseCategories = getSharedPreferences("ExerciseAPI", Context.MODE_PRIVATE);
            if (!ExerciseCategories.contains("Categories-" + LastCategory)){ //if its not already there
                SharedPreferences.Editor editor = ExerciseCategories.edit();
                editor.putString("Categories-" + LastCategory, result.toString());
                editor.commit();
            }

        } catch (Exception e) { //catch any exceptions, json, storage etc
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, exercises);
        listView.setAdapter(adapter);//create list

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                returnText(((TextView)view).getText().toString());
            }
        });
    }

    public void ReturnJsonLocationInfo(JSONArray result){ //just for interface implementation
    }

}

