package uk.ac.lincoln.a16601608students.workoutapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

//Written using https://developer.android.com/training/data-storage/sqlite#java

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Workouts.db";
    private static final String TABLE_NAME = "Exercises";
    private static final String COLUMN_NAME_ONE = "ID";
    private static final String COLUMN_NAME_TWO = "Exercise";
    private static final String COLUMN_NAME_THREE = "Reps";
    private static final String COLUMN_NAME_FOUR = "Sets";
    private static final String COLUMN_NAME_FIVE = "WorkoutName";
    private static final String COLUMN_NAME_SIX = "DayNumber";

    public static String SQL_CREATE_TABLE() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_NAME_ONE + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NAME_TWO + " TEXT," +
                COLUMN_NAME_THREE + " INTEGER," +
                COLUMN_NAME_FOUR + " INTEGER," +
                COLUMN_NAME_FIVE + " TEXT," +
                COLUMN_NAME_SIX + " INTEGER," +
                "FOREIGN KEY ("+COLUMN_NAME_FIVE+") REFERENCES Workouts(" + COLUMN_NAME_FIVE + "))";
    }

    public void createWorkoutsTable(){
        SQLiteDatabase db = this.getWritableDatabase(); //get db
        String q = "CREATE TABLE " + "Workouts" + " (" +
                COLUMN_NAME_FIVE + " TEXT PRIMARY KEY)";
        try{  //try to create workouts table
            db.execSQL(q); //create workouts table
        }catch (SQLException e){ //already exist, so just continue

        }

        try{
            db.execSQL(SQL_CREATE_TABLE()); //create Exercises table also here
        }catch (SQLException e){ //may already exist, so just continue

        }
    }

    public static String SQL_DELETE_ENTRIES(String tableName){ //for some reason required?
        return "DROP TABLE IF EXISTS " + tableName; }

    public SQLiteHelper(Context context) { //creates database when constructor called
        super(context, DATABASE_NAME, null, 1); //create database
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long createNewWorkout(String workoutName){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_FIVE, workoutName);
        //long newRowId =
        return db.insert("Workouts", null, values);//will return -1 if already exists
    }

    public long insert(String DayNumber, String exercise, int reps, int sets, String WorkoutName ) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase(); //get db

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //values.put(COLUMN_NAME_ONE, number); //autoincrements, therefore don't need value here
        values.put(COLUMN_NAME_TWO, exercise);
        values.put(COLUMN_NAME_THREE, reps);
        values.put(COLUMN_NAME_FOUR, sets);
        values.put(COLUMN_NAME_FIVE, WorkoutName);
        values.put(COLUMN_NAME_SIX, DayNumber);

        // Insert the new row, returning the primary key value of the new row
        //null = do not insert a row if there are no values
        long newRowId = db.insert(TABLE_NAME, null, values);
        //will return -1 if there was an error inserting value

        return newRowId;

    }

    public long deleteRow (String ID){
        SQLiteDatabase db = this.getWritableDatabase(); //get db

        return db.delete(TABLE_NAME, COLUMN_NAME_ONE + "=" + ID, null);

    }

    //returns data of a workout by day number
    public Cursor displayTable(String WorkoutName, String DayNumber){ //return cursor to table
        SQLiteDatabase db = this.getReadableDatabase();

        String[] selectionArgs = {WorkoutName,DayNumber};

        Cursor  cursor = db.rawQuery("Select * from " + TABLE_NAME + " where " + COLUMN_NAME_FIVE + " = ? " + " AND " + COLUMN_NAME_SIX + " = ?", selectionArgs);

        return cursor;
    }

    public Cursor getAllWorkoutDays(String WorkoutName){ //return cursor to table with all unique days of a workout
        SQLiteDatabase db = this.getReadableDatabase();

        String[] selectionArgs = {WorkoutName};

        try{  //try to get all workout days
            return db.rawQuery("Select DISTINCT " + COLUMN_NAME_SIX + " from " + TABLE_NAME + " where " + COLUMN_NAME_FIVE + " = ? " , selectionArgs);
        }catch (SQLException e){ //no days, return null
            return null;
        }
    }

    public Cursor displayWorkouts(){
        SQLiteDatabase db = this.getReadableDatabase();

        try{  //try to get workouts table
            return db.rawQuery("Select * from Workouts " , null);
        }catch (SQLException e){ //no table, return null
            return null;
        }
    }

    public int updateTable(String columnName, String data, String ID){

        SQLiteDatabase db = this.getReadableDatabase();

        //new value for coloumns
        ContentValues values = new ContentValues();
        values.put(columnName, data);

        String selection = COLUMN_NAME_ONE + " = ?";
        String[] selectionArgs = { ID };

        int count = db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count;
    }

}


