package uk.ac.lincoln.a16601608students.workoutapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class NewWorkoutsFragment extends Fragment {
    private int DayNumber = 0;
    private String currentWorkout; //current workout this new workout screen is adding to

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_newworkout, container, false);

        enterWorkoutNameDialog(); //get new workout name

        //explicitly link method with button, as xml links don't work with fragments
        //from https://stackoverflow.com/questions/38942843/android-method-is-never-used-warning-for-onclick-method
        Button addDayButton = rootView.findViewById(R.id.addDayButton);
        addDayButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addDay(v);
            }
        });

        return rootView;
    }

    public void  enterWorkoutNameDialog(){
        //from https://stackoverflow.com/questions/10903754/input-text-dialog-android

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Workout name:(Must be unique)");
        builder.setCancelable(false); //stop it from being closed

        // Set up the input
        final EditText input = new EditText(this.getContext());
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentWorkout = input.getText().toString();
                if (currentWorkout.length() == 0){ //try again if they enter nothing
                    enterWorkoutNameDialog();
                } else{
                    SQLiteHelper mDbHelper = new SQLiteHelper(getContext());
                    long check = mDbHelper.createNewWorkout(currentWorkout); //add to workouts table
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if cancel clicked, dont let them add a workout (switch back to view workouts)
                getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new WorkoutsFragment()).commit();
            }
        });

        builder.show();
    }

    public void createRowButton(final String tableName){
        LinearLayout allDayTablesLayout = getView().findViewById(R.id.dayTableLayout); //get tables layout

        //create add row button
        Button addRowBtn = new Button(this.getContext());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        addRowBtn.setLayoutParams(rowParams);
        addRowBtn.setText("Add Row +");
        addRowBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                addRow(v,tableName);
            }
        });
        //add to layout
        allDayTablesLayout.addView(addRowBtn);

    }

    public void displayTableToScreen(final String dayNumber) {

        //each table and data row has Tag, if tag exists in the view, we dont create new views for it

        SQLiteHelper mDbHelper = new SQLiteHelper(getContext()); //get db to work with
        Cursor cursor = mDbHelper.displayTable(currentWorkout, dayNumber); //return cursor with table info

        TableLayout dayTableLayout;
        //get current all tables layout
        LinearLayout allDayTablesLayout = getView().findViewById(R.id.dayTableLayout);

        //create row layout params for later
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT);

        String tableTag = currentWorkout + "day" + dayNumber;
        //create new table, and set layout for this new day table
        if (getView().findViewWithTag(tableTag) == null){ //if no table found with tablename
            dayTableLayout = new TableLayout(this.getContext());
            dayTableLayout.setStretchAllColumns(true);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT );
            dayTableLayout.setWeightSum(5);
            dayTableLayout.setLayoutParams(params);
            dayTableLayout.setTag(tableTag); //set tag, to find it later

           //add table to current all tables layout
            allDayTablesLayout.addView(dayTableLayout);

            // displaying titles: //

            //create a row layout
            TableRow newRow = new TableRow(this.getContext());
            newRow.setGravity(Gravity.CENTER);
            newRow.setLayoutParams(rowParams);

            int i = 0;
            for (String colName : cursor.getColumnNames()){ //for each column in table, add headings
                i++;
                if ((i >= 2) && (i < 5)){ //skip first column and last 2 (only dispaly exercise,sets,reps)
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

            //add empty column in place for del button
            TextView col = new TextView(getContext());
            col.setText(" ");
            newRow.addView(col);

            dayTableLayout.addView(newRow); //add row to table

        } else{ //table already exists
            dayTableLayout = getView().findViewWithTag(tableTag);
        }

        //displaying data (content):

        if (cursor.getCount() > 0){        //if data exists to be added
            cursor.moveToFirst();   //move to first item
            do{ //foreach value in cursor

                //if this datarow doesnt exist
                if (getView().findViewWithTag(tableTag + "datarow" + cursor.getString(0)) == null){

                    //create a row layout
                    TableRow newRow = new TableRow(this.getContext());
                    newRow.setGravity(Gravity.CENTER);
                    newRow.setLayoutParams(rowParams);
                    newRow.setTag(tableTag + "datarow" + cursor.getString(0));

                    //only display 3 columns, set i to start at 1 to skip ID
                    for (int i = 1; i < 4; i++){ //have to set individually to pass into customTextWatcher
                       if ( i == 1){ //exercise name, keep normal text view
                            TextView col = new TextView(getContext()); //final in temp for onclick
                            col.setTextSize(13); //otherwise text and editViews are different sizes
                            col.setText(cursor.getString(i));
                            col.setBackground(this.getContext().getDrawable(R.drawable.columncontentbox));  //add cell properties
                            final int colID = View.generateViewId(); //generate ID for returning data to it from SelectExercise
                            col.setId(colID );
                            col.setLayoutParams(new TableRow.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT, 3)); //special weight for large column
                            col.setClickable(true);
                            col.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v){
                                    getExercise(colID); //pass in ID to selectExercise
                                }
                            });

                            newRow.addView(col);
                        }
                        else{ //third and fourth column, make typeable numbers (have to be ints)
                            EditText col = new EditText(getContext());
                            col.setInputType(InputType.TYPE_CLASS_NUMBER);
                            col.setTextSize(13); //otherwise text and editViews are different sizes
                            col.setText(cursor.getString(i));
                            col.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT)); //hardcode height for 2 rows worth
                            col.setBackground(this.getContext().getDrawable(R.drawable.columncontentbox));  //add cell properties
                            col.addTextChangedListener(new CustomTextWatcher(col, cursor.getColumnName(i), cursor.getString(0))); //use customTextWatcher
                            newRow.addView(col);
                        }

                    }

                    TextView delRowBtn = new TextView(getContext()); //final in temp for onclick
                    delRowBtn.setTextSize(20); //otherwise text and editViews are different sizes
                    delRowBtn.setText(" - ");
                    delRowBtn.setGravity(Gravity.CENTER);
                    delRowBtn.setLayoutParams(new TableRow.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT));
                    delRowBtn.setClickable(true);
                    delRowBtn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            delRow(v);
                        }
                    });

                    newRow.addView(delRowBtn);

                    dayTableLayout.addView(newRow); //add column layout to tables list
                }

            } while(cursor.moveToNext()); //need to put this after so Do runs first otherwise it skips first item
        }

    }

    //add row
    public void addRow(View view,String tableName){
        SQLiteHelper mDbHelper = new SQLiteHelper(getContext()); //get db to work with

        //add row to current daytable
        long test = mDbHelper.insert(tableName, "tap to enter ex", 5, 4, currentWorkout );

        if (test == -1){ //error: could not insert row
            Toast.makeText(this.getContext(), "error: could not insert row", Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(this.getContext(), "Row inserted", Toast.LENGTH_LONG).show();
            //now display table after adding a row is successful
            displayTableToScreen(tableName);
        }
    }

    public void delRow(View view){
        SQLiteHelper mDbHelper = new SQLiteHelper(getContext());

        View parent = (View)view.getParent();           //get row
        String rowTag = (String)parent.getTag();    //extract the row tag
        String rowNumber = rowTag.substring(rowTag.indexOf("datarow")+7); //extract the row number (what ever is after 'datarow')

        long test = mDbHelper.deleteRow(rowNumber);

        if (test == -1){ //error: could not insert row
            Toast.makeText(this.getContext(), "error: could not delete row", Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(this.getContext(), "Row deleted", Toast.LENGTH_LONG).show();
            //remove row from table after deleting from db
            ViewGroup Table = (ViewGroup)parent.getParent();
            Table.removeView(parent);
        }
    }

    public void addDay(View view){
        DayNumber += 1;
        String dayNumber = "" + DayNumber;
        if (DayNumber <= 7){ //only add table if we have less than 7
            SQLiteHelper mDbHelper = new SQLiteHelper(getContext()); //get db to work with
            mDbHelper.insert(dayNumber, "tap to enter ex", 5, 4 , currentWorkout); //insert starting row

            //now display days after adding a table is successful
            displayTableToScreen(dayNumber);
            createRowButton(dayNumber);
        }
    }

    public void getExercise(int id){
        int PICK_CONTACT_REQUEST = 1; //request code

        Intent intent = new Intent(this.getContext(), SelectExercise.class);
        intent.putExtra("ID", id);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //on selectExercise result

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("resultText");
                int ID = data.getIntExtra("resultID", 1);

                TextView col = getView().findViewById(ID);  //find the Exercise column they tapped
                col.setText(result);                        //set the exercise they chose
                SQLiteHelper mDbHelper = new SQLiteHelper(getContext()); //update in sql

                View parent = (View)col.getParent();        //parent is the row
                String rowTag = (String)parent.getTag();    //extract the row tag
                String rowNumber = rowTag.substring(rowTag.indexOf("datarow")+7); //extract the row number (what ever is after 'datarow')

                mDbHelper.updateTable( "Exercise", result, rowNumber);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //if there's no result
            }
        }
    }//onActivityResult

    private class CustomTextWatcher implements TextWatcher {
        private EditText mEditText;
        private String colName;
        private String ID;

        public CustomTextWatcher(EditText e, String columnName, String id) {
            mEditText = e;
            colName = columnName;
            ID = id;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            //update db
            SQLiteHelper mDbHelper = new SQLiteHelper(getContext());
            int n = mDbHelper.updateTable(colName, s.toString(), ID);
        }
    }
}
