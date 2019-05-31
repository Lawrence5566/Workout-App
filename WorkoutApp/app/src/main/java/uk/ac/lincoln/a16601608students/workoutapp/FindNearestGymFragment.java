package uk.ac.lincoln.a16601608students.workoutapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;

public class FindNearestGymFragment extends Fragment implements ReturnJson {
    LocationManager locationManager;
	String APIKEY = "add api key here";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_findgym, container, false);

        Button ReqLocBtn = rootView.findViewById(R.id.ReqLocBtn);
        ReqLocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocationPerms();
            }
        });

        //on opening this screen, check for permissions:
        addLocationPerms();

        return rootView;
    }

    //// location ////
    //////////////////

    private LocationListener locationListener = new LocationListener() { //updates location in location manager for us
        @Override
        public void onLocationChanged(final Location loc) {
            double latitude=loc.getLatitude();
            double longitude=loc.getLongitude();
            //location = latitude+longitude+"";
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void getLocation(){

        // acquire a reference to the system Location Manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) //check if we have perms for location manager
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so ask for them

            addLocationPerms();
        } else {

            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 2000, 10, locationListener); //start looking for location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // create a few new variable to get and store the lat/long coordinates of last known location
            double lat;
            double longi;

            // check if a last known location exists
            if (lastKnownLocation == null) {
                // if no last location is available set lat/long to zero
                lat = 0;
                longi = 0;
            } else {
                // if last location exists then get/set the lat/long
                lat = lastKnownLocation.getLatitude();
                longi = lastKnownLocation.getLongitude();
            }

            String location = lat + ","+ longi;

            //use location data
            new ParseLocJson(this, location).execute();
        }
    }

    public void addLocationPerms(){
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { //on result of calender perms ask
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    // display short notification stating permission granted
                    Toast.makeText(this.getContext(), "Permission granted to access Location!", Toast.LENGTH_SHORT).show();

                    //hide add perms button
                    Button ReqLocBtn = getView().findViewById(R.id.ReqLocBtn);
                    ReqLocBtn.setVisibility(LinearLayout.GONE);

                    //run get location task
                    getLocation();

                } else {
                    // permission denied, display stuff to let them try add again
                    //make sure button is displayed
                    Button ReqLocBtn = getView().findViewById(R.id.ReqLocBtn);
                    ReqLocBtn.setVisibility(LinearLayout.VISIBLE);

                }
                return;
            }
        }
    }

    //// JSON ////
    //////////////

    @Override
    public void ReturnJson(JSONArray result) {
    }

    @Override
    public void ReturnJsonExercises(JSONArray result) {
    }

    @Override
    public void ReturnJsonLocationInfo(JSONArray result){

        if (result == null){
            Toast.makeText(this.getContext(), "please connect to the internet and try again", Toast.LENGTH_LONG).show();
        } else {

            ListView listView = getView().findViewById(R.id.localGymsListView); //find list view to add data too

            ArrayList<Place> places = new ArrayList<Place>();

            // loop through json array "results"
            for (int i = 0; i < result.length(); i++) {
                try {
                    JSONObject json_obj = result.getJSONObject(i);
                    if (json_obj != null) {
                        //extract "name" and "id" of exercise category from object
                        String title = (json_obj.getString("name"));
                        JSONArray photos = json_obj.getJSONArray("photos"); //some don't have photos, so we won't use them
                        String imgRef = photos.getJSONObject(0).getString("photo_reference"); //photos contains only 1 object
                        String desc = (json_obj.getString("vicinity"));
                        places.add(new Place(title, imgRef, desc)); //add new place to places array
                    }
                } catch (JSONException e) { //using getJSONObject so need catch
                    e.printStackTrace();
                    //if Exception for this object its likely we couldn't get a image so skip
                }
            }

            if (places.size() == 0) { //if no places:
                TextView tv1 = new TextView(this.getContext());
                tv1.setText("No places nearby");
                ((ViewGroup) listView.getParent()).addView(tv1);
            } else {
                PlacesAdapter adapter = new PlacesAdapter(getContext(), places);
                listView.setAdapter(adapter);
            }
        }

    }

    public class Place{
        String Title;
        String ImgLink;
        String Desc;

        public Place(String title, String imgRef, String desc){
            Title = title;
            ImgLink = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference=" + imgRef + "&key=" + APIKEY;
            Desc = desc;
        }

    }

    //from http://web.archive.org/web/20120802025411/http://developer.aiwgame.com/imageview-show-image-from-url-on-android-4-0.html for downloading image
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> { //downloads images from url
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private class PlacesAdapter extends ArrayAdapter<Place>{

        public PlacesAdapter (Context context, ArrayList<Place> places){
            super(context, 0, places);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //get place for this position
            Place place = getItem(position);
            //check if existing view is being refused
            if (convertView == null){
                convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.loclistrow, parent, false);
            }

            TextView placeTitle = (TextView) convertView.findViewById(R.id.placeTitle);
            TextView placeDesc = (TextView) convertView.findViewById(R.id.placeDesc);
            //ImageView placeIcon = (ImageView) convertView.findViewById(R.id.placeIcon);

            placeTitle.setText(place.Title);
            placeDesc.setText(place.Desc);
            //set resource for img using asyc task:

            new DownloadImageTask((ImageView) convertView.findViewById(R.id.placeIcon)).execute(place.ImgLink);

            return convertView;
        }
    }
}




