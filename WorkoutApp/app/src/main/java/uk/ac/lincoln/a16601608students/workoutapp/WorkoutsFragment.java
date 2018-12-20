package uk.ac.lincoln.a16601608students.workoutapp;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WorkoutsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workouts, container, false);

        displayWorkouts(rootView);

        return rootView;
    }

    public void displayWorkouts(View rootView) {
        LinearLayout allWorkoutsLayout = rootView.findViewById(R.id.WorkoutsLinearLayout);

        SQLiteHelper mDbHelper = new SQLiteHelper(getContext()); //get db to work with
        Cursor cursor = mDbHelper.displayWorkouts(); //return cursor for * table info

        if (cursor.getCount() > 0) {        //if data exists to be added
            cursor.moveToFirst();   //move to first row in table
            do{

                TextView workout = new TextView(getContext());
                workout.setTextSize(30);
                workout.setText(cursor.getString(0));
                workout.setTextColor(Color.BLACK);
                workout.setClickable(true);
                workout.setTag(cursor.getString(0));
                workout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        DisplayWorkoutContents(v.getTag().toString()); //pass in Tag/workout name to display its contents
                    }
                });

                allWorkoutsLayout.addView(workout);

            } while(cursor.moveToNext());
        }
    }

    private void DisplayWorkoutContents(String WorkoutName) {
        LinearLayout allWorkoutsLayout = getView().findViewById(R.id.WorkoutsLinearLayout);

        allWorkoutsLayout.removeAllViews(); //remove items from layout

        SQLiteHelper mDbHelper = new SQLiteHelper(getContext());

        //loop foreach day:
        int maxDay = mDbHelper.getAllWorkoutDays(WorkoutName).getCount(); //get number of day numbers for this workout from sql

        for (int i = 1; i <= maxDay; i++ ) {
            displayTableToScreen(WorkoutName, "" + i);
        }

    }

    public void displayTableToScreen(String currentWorkout, String dayNumber) {

        //each table and data row has Tag, if tag exists in the view, we don't create new views for it

        SQLiteHelper mDbHelper = new SQLiteHelper(getContext()); //get db to work with
        Cursor cursor = mDbHelper.displayTable(currentWorkout, dayNumber); //return cursor with table info

        //create all layout to place tables in
        LinearLayout allDayTablesLayout = getView().findViewById(R.id.WorkoutsLinearLayout);

        //create row layout params for later
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT);

        //create new table, and set layout for this new day table
        TableLayout dayTableLayout = new TableLayout(this.getContext());
        dayTableLayout.setStretchAllColumns(true);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT );
        dayTableLayout.setPadding(1,15,1,15);
        dayTableLayout.setWeightSum(5);
        dayTableLayout.setLayoutParams(params);

        //add table to current all tables layout
        allDayTablesLayout.addView(dayTableLayout);

        // displaying titles: //

        //create a row layout
        TableRow newRow = new TableRow(this.getContext());
        newRow.setGravity(Gravity.CENTER);
        newRow.setLayoutParams(rowParams);

        int i=0;
        for (String colName : cursor.getColumnNames()){ //for each column in table, add headings
            i++;
            if ((i >= 2) && (i < 5)){ //skip first column and last 2 (only display exercise,sets,reps)
                TextView col = new TextView(getContext());
                col.setText(colName);
                col.setBackground(this.getContext().getDrawable(R.drawable.columntitlebox));  //add cell properties
                if (i == 2){
                    col.setLayoutParams(new TableRow.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT, 3)); //special weight for large column
                } else {
                    col.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }

                newRow.addView(col);
            }
        }

        dayTableLayout.addView(newRow); //add row to table

        //displaying data (content):

        if (cursor.getCount() > 0){        //if data exists to be added
            cursor.moveToFirst();   //move to first item
            do{ //foreach value in cursor
                //create a row layout
                TableRow newRow2 = new TableRow(this.getContext());
                newRow.setGravity(Gravity.CENTER);
                newRow.setLayoutParams(rowParams);

                //only display 3 columns, set i to start at 1 to skip ID
                for (int n = 1; n < 4; n++){ //have to set individually to pass into customTextWatcher
                    TextView dataCol = new TextView(getContext()); //final in temp for onclick
                    dataCol.setTextSize(13); //otherwise text and editViews are different sizes
                    dataCol.setText(cursor.getString(n));
                    dataCol.setBackground(this.getContext().getDrawable(R.drawable.columncontentbox));  //add cell properties
                    dataCol.setLayoutParams(new TableRow.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT, 3)); //special weight for large column
                    newRow2.addView(dataCol);

                }

                dayTableLayout.addView(newRow2); //add column layout to tables list

            } while(cursor.moveToNext()); //need to put this after so Do runs first otherwise it skips first item
        }

    }
}
