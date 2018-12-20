package uk.ac.lincoln.a16601608students.workoutapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SetRemindersFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reminders, container, false);

        //on opening this screen, check for permissions first and only run content if we have perms
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so ask for them

            addCalenderPerms();
        }

        Button AddEventBtn = rootView.findViewById(R.id.AddEventBtn);
        AddEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventIntent(); //write simple event to calendar
            }
        });

        return rootView;
    }

    public void eventIntent() { //calender event intent
        String label = ((EditText)getView().findViewById(R.id.editTextLabel)).getText().toString();
        String time = ((EditText)getView().findViewById(R.id.editTextTime)).getText().toString();
        String date = ((EditText)getView().findViewById(R.id.editTextDate)).getText().toString();
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
            formatter.setLenient(false);
            Date parsedDate = formatter.parse(date + " " + time); //try to parse date

            //create calendar Intent and set values
            Intent calendarIntent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(parsedDate); //set begin time  to equal parsed date
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(parsedDate);
            endTime.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
            calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
            calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis());
            calendarIntent.putExtra(CalendarContract.Events.TITLE, label);
            calendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Gym");

            String rule = "FREQ=WEEKLY;BYDAY=";
            boolean check = false;
            LinearLayout Checkboxes = getView().findViewById(R.id.checkBoxes);
            for (int i = 0; i < Checkboxes.getChildCount(); i++){
                CheckBox cb = (CheckBox)Checkboxes.getChildAt(i);
                if(cb.isChecked()){
                    check = true;
                    String day = cb.getText().toString();
                    switch (day){
                        case "Monday": rule = rule + "MO,";
                            break;
                        case "Tuesday": rule = rule + "TU,";
                            break;
                        case "Wednesday": rule = rule + "WE,";
                            break;
                        case "Thursday": rule = rule + "TH,";
                            break;
                        case "Friday": rule = rule + "FR,";
                            break;
                        case "Saturday": rule = rule + "SA,";
                            break;
                        case "Sunday": rule = rule + "SU,";
                            break;
                    }
                }
            }
            if (check){//remove last character and add rule if we had a checkbox
                rule = rule.substring(0,rule.length() - 1);
                calendarIntent.putExtra(CalendarContract.Events.RRULE, rule);
            }

            startActivity(calendarIntent);

        } catch (ParseException parseEx){
            Toast.makeText(this.getContext(), "ERROR: Enter date and time in correct format", Toast.LENGTH_LONG).show();
        }

    }

    //permissions stuff - ask only if we need to
    public void addCalenderPerms(){
        requestPermissions(
                new String[]{Manifest.permission.WRITE_CALENDAR},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { //on result of calender perms ask
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the work!
                    // display short notification stating permission granted
                    Toast.makeText(this.getContext(), "Permission granted to access Calender!", Toast.LENGTH_SHORT).show();

                    //show the add reminders stuff
                    LinearLayout remindersLayout = getView().findViewById(R.id.remindersLayout);
                    remindersLayout.setVisibility(LinearLayout.VISIBLE);

                    //hide the perms stuff (if visible)
                    LinearLayout addPermsLayout = getView().findViewById(R.id.addPermsLayout);
                    addPermsLayout.setVisibility(LinearLayout.GONE);


                } else {
                    // permission denied, display stuff to let them try add again

                    LinearLayout remindersLayout = getView().findViewById(R.id.remindersLayout);
                    remindersLayout.setVisibility(LinearLayout.GONE); //hide stuff so they can't add reminders

                    LinearLayout addPermsLayout = getView().findViewById(R.id.addPermsLayout);
                    addPermsLayout.setVisibility(LinearLayout.VISIBLE); //show layout
                    if (addPermsLayout.getChildCount() < 1) { //if it has nothing
                        TextView tv1 = new TextView(this.getContext());
                        tv1.setText("Permission is not granted - we need this to access your calendar!");
                        Button btn1 = new Button(this.getContext());
                        btn1.setText("add Perms");
                        btn1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addCalenderPerms();
                            }
                        });

                        addPermsLayout.addView(tv1);
                        addPermsLayout.addView(btn1);
                    }

                }
                return;
            }
        }
    }

}
