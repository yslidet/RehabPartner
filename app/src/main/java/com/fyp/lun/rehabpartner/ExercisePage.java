package com.fyp.lun.rehabpartner;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ExercisePage extends Activity implements SensorEventListener {

    /* var for accelerometer sensors */
    private SensorManager accSensorManager;
    private Sensor senAccel;
    private double acceX = 0.0, acceY = 0.0, acceZ = 0.0;

    /* var for gyroscope sensors */
    private SensorManager gyroSensorManager;
    private Sensor senGyro;
    private double gyroX = 0.0, gyroY = 0.0, gyroZ = 0.0;

    /* var for magnetometer sensors */
    private SensorManager magSensorManager;
    private Sensor senMag;
    private double magX = 0.0, magY = 0.0, magZ = 0.0;

    /* ArrayList to store CF's data */
    double[] accel = {0.0,0.0,0.0};
    double[] gyro = {0.0,0.0,0.0};
    double[] magnet = {0.0,0.0,0.0};
    double[] prevAccel = {0.0,0.0,0.0};
    double[] prevGyro = {0.0,0.0,0.0};
    double[] prevHPFResults = {0.0,0.0,0.0};
    double[] prevResults = {0.0,0.0,0.0};
    double[] results = {0.0,0.0,0.0};

    /* Variables to hold data of repetitions */
    double initialAngle = 0, rangeAngle = 0.0;
    double minRepAngle = 0.0, maxRepAngle = 0.0;

    /* Variables to hold incorrect movement */
    double initialRollAngle = 0.0, initialYawAngle = 0.0;
    int errorOnRoll = 0, errorOnYaw = 0, totalError = 0;
    int reps = 0;

    /* To lock timer task */
    int cfFlag = 0, firstMin = 0, state = 0, repFlag = 0;
    int msCounter = 0, repText = 0, elapsedTime =0;
    int up = 0, down = 0, displayState = 1, hold = 0;
    int repSetter = 0;

    /* ArrayList to store sensor's data and metadata */
    ArrayList<String> list = new ArrayList<String>();
    ArrayList<String> metaList = new ArrayList<String>();
    String data = null;
    String metaData = null;

    /* Formatting of display */
    DecimalFormat d = new DecimalFormat("#.###");
    Calendar c;
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");

    /* Timer to track data */
    Timer timer;
    TimerTask timerTask;

    /* Handler */
    final Handler handler = new Handler();

    /* TextView Variables */
    private TextView repCounts, levelTV;

    /* Buttons Variables*/
    Button backBtn, stopBtn;

    /* previous activity's data */
    private String mode = "", sides = "Right";
    private int repsNeeded = 0;
    private boolean hasLevel = false, hasReps = false, hasSides = false;
    Files nFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_page);

        //------------------------------Setting up Sensors Managers ---------------------------------//
        accSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccel = accSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accSensorManager.registerListener(this, senAccel, SensorManager.SENSOR_DELAY_NORMAL);

        gyroSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senGyro = gyroSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroSensorManager.registerListener(this, senGyro, SensorManager.SENSOR_DELAY_NORMAL);

        magSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senMag = magSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magSensorManager.registerListener(this, senMag, SensorManager.SENSOR_DELAY_NORMAL);

        //--------------------------- Initialize all arrays to zeros -------------------------------//
        Arrays.fill(accel, 0.0);
        Arrays.fill(gyro, 0.0);
        Arrays.fill(magnet, 0.0);
        Arrays.fill(prevAccel, 0.0);
        Arrays.fill(prevGyro, 0.0);

        /* Initialized TextView to display results */
        repCounts = (TextView) this.findViewById(R.id.repCount);
        levelTV = (TextView) this.findViewById(R.id.levelTV);

        /* Buttons Initialization */
        backBtn = (Button) findViewById(R.id.backBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);

        /* Buttons listener */
        backBtn.setOnClickListener((new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentBundle = new Intent(ExercisePage.this, SeatedKneeExtension.class);
                Bundle bundle = new Bundle();
                bundle.putString("Level", mode);
                bundle.putInt("Reps", reps);
                bundle.putString("Sides", sides);
                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
                stopTimerTask();
            }
        }));

        stopBtn.setOnClickListener((new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(ExercisePage.this, MainActivity.class));
            }
        }));


        /* Initial sensors database headings */
        data = "timestamp, ax, ay, az, gx, gy, gz, mx, my, mz, roll, pitch, yaw" + "\n";
        list.add(data);

        nFile = new Files(this);

        /* Getting previous activity's data */
        Intent intentExtras = getIntent();
        Bundle extrasBundle = intentExtras.getExtras();
        if(!extrasBundle.isEmpty())
        {
            hasLevel = extrasBundle.containsKey("Level");
            hasReps = extrasBundle.containsKey("Reps");
            hasSides = extrasBundle.containsKey("Sides");
        }
        if(hasLevel)
        {
            mode = extrasBundle.getString("Level");
            levelTV.setText(mode);
        }
        if(hasReps)
        {
            repsNeeded = extrasBundle.getInt("Reps");
        }
        if(hasSides)
        {
            sides = extrasBundle.getString("Sides");
        }

        startTimer();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        //Getting Accelerometer values
        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceX = sensorEvent.values[0];
            acceY = sensorEvent.values[1];
            acceZ = sensorEvent.values[2];
        }

        //Getting Gyroscope values
        if(mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroX = sensorEvent.values[0];
            gyroY = sensorEvent.values[1];
            gyroZ = sensorEvent.values[2];
        }

        //Getting Magnetic values
        if(mySensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magX = sensorEvent.values[0];
            magY = sensorEvent.values[1];
            magZ = sensorEvent.values[2];
        }
    }

    /* Start timer function */
    private void startTimer() {
        //sets a new timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 100);
    }

    /* Stop timer function */
    private void stopTimerTask() {
        //stop the timer
        if(timer != null)
        {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /* Task to run based on time */
    private void initializeTimerTask() {
        timerTask = new TimerTask(){
            public void run(){

                handler.post(new Runnable(){
                    public void run(){
                        if(cfFlag == 0) {
                            // run complementary filter
                            cfProcess();

                            // to keep track of time activity started in seconds
                            msCounter++;

                            // to ensure that cf process finish running before starting new tasks
                            cfFlag++;

                            //indicates 1 second had passed by
                            if(msCounter%10 == 0) {
                                // Keep track of total elapsedTime
                                elapsedTime += 1;
                                // repText to ensure correct display of instructions
                                repText += 1;
                                hold += 1;

                                // After initial 5secs, sensors reading to be set as initial readings
                                if(elapsedTime == 5) {
                                    setInitial();
                                    initialRollAngle = results[2] * 180/Math.PI;
                                    initialYawAngle = results[0] * 180/Math.PI;
                                }
                                // To stop task after required amount of time
                                if(elapsedTime == repsNeeded*20)
                                {
                                    timerTask.cancel();
                                    timer.cancel();
                                    saveMetaData();
                                    saveMetaList();
                                    saveData();
                                    switchActivity();
                                }
                                else
                                {
                                    // Displaying of instruction every 5 seconds
                                    if(repText%5 == 0) {
                                        switch (displayState) {
                                            case 1:
                                                repCounts.setText("Up");
                                                break;
                                            case 2: repCounts.setText("Hold");
                                                up = 1;
                                                break;
                                            case 3: repCounts.setText("Down");
                                                //elapsedTime = 1;
                                                break;
                                            case 4: repCounts.setText("Hold");
                                                down = 1;
                                                break;
                                            default:
                                                break;
                                        }
                                        if(displayState == 4) {
                                            // Resetting to first instruction
                                            displayState = 1;
                                        }
                                        else {
                                            // Changing to next instruction
                                            displayState +=1;
                                        }
                                        // Reset timer to ensure it is always 5secs
                                        repText = 0;
                                    }
                                    // If not at 5 secs mark, continue to calculate movement
                                    else {
                                        repCounts.setText("" + repText);
                                        if(up == 1) {
                                            detectRollError();
                                            detectYawError();
                                            detectChange();
                                            up = 0;
                                        }
                                        if(down == 1) {
                                            setInitial();
                                            down = 0;

                                        }
                                    }
                                    msCounter = 0; // to reset timer
                                }

                            }

                        }
                    }
                });
            }
        };
    }

    /* Running complementary filter process */
    private void cfProcess() {
        // remember x axis must swap with z axis
        int j,k;

        ComplementaryFilter cf = new ComplementaryFilter();

        if(state == 0)
        {
            results = cf.process(accel,prevAccel, magnet, gyro, prevResults, prevHPFResults);

            for(int i = 0; i < 3; i++)
            {
                prevResults[i] = results[i];
            }
            j=0;
            for(int i = 3; i < 6; i++)
            {
                prevHPFResults[j] = results[i];
                j++;
            }
            k=0;
            for(int i = 6; i < 9; i++)
            {
                prevAccel[k] = results[i];
                k++;
            }
            cfFlag--;
            state = 1;
        }
        else
        {
            accel = cf.android2XSEN(acceX, acceY, acceZ);
            gyro = cf.android2XSEN(gyroX, gyroY, gyroZ);
            magnet = cf.android2XSEN(magX, magY, magZ);
            results = cf.process(accel,prevAccel, magnet, gyro, prevResults, prevHPFResults);

            for(int i = 0; i < 3; i++)
            {
                prevResults[i] = results[i];
            }
            j=0;
            for(int i = 3; i < 6; i++)
            {
                prevHPFResults[j] = results[i];
                j++;
            }
            k=0;
            for(int i = 6; i < 9; i++)
            {
                prevAccel[k] = results[i];
                k++;
            }

            cfFlag--;
        }
        saveList();

    }

    /* Application on pause */
    protected void onPause() {
        super.onPause();
        accSensorManager.unregisterListener(this);
        gyroSensorManager.unregisterListener(this);
        magSensorManager.unregisterListener(this);
        stopTimerTask();
    }

    /* Application on resume */
    protected void onResume() {
        super.onResume();
        accSensorManager.registerListener(this, senAccel, SensorManager.SENSOR_DELAY_NORMAL);
        gyroSensorManager.registerListener(this, senGyro, SensorManager.SENSOR_DELAY_NORMAL);
        magSensorManager.registerListener(this, senMag, SensorManager.SENSOR_DELAY_NORMAL);
        startTimer();
    }

    /* Saving sensor's raw and filtered data into a list */
    private void saveList() {
        // logging the data by adding all the raw and calculated data into a list
        Long tsLong = System.currentTimeMillis();

        data = tsLong.toString() + "," + Double.toString(accel[0]) + "," + Double.toString(accel[1]) + "," + Double.toString(accel[2]) + "," +
                Double.toString(gyro[0]) + "," + Double.toString(gyro[1]) + "," + Double.toString(gyro[2]) + "," +
                Double.toString(magnet[0]) + "," + Double.toString(magnet[1]) + "," + Double.toString(magnet[2]) + "," +
                Double.toString(results[2]*180 / Math.PI) + "," + Double.toString(results[1]*180 / Math.PI) + "," + Double.toString(results[0]*180 / Math.PI) + "," + "\n";
        list.add(data);
    }

    /* Saving sensor's raw and filtered data into a .txt file */
    private void saveData(){
        // Setting up variables used for creating a file
        File internalFile;
        String fileName = "sensorData_" + String.valueOf(System.currentTimeMillis() / 1000) +"_dev.txt";
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File dir = contextWrapper.getExternalFilesDir("MyFileStorage");
        internalFile = new File(dir, fileName);

        // writing array list of data into the text file
        try
        {
            FileOutputStream outputStream = new FileOutputStream(internalFile);
            for(String s: list)
            {
                outputStream.write(s.getBytes());
            }
            outputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    /* Set initial angle */
    private void setInitial()
    {
        //results[2] = x-axis, results[1] = y-axis, results[0] = z-axis
        initialAngle = results[1] * 180/Math.PI;
    }

    /* Detecting how many roll angle errors from rehab */
    private void detectRollError()
    {
        // Setting the range of error
        double errorRange = 5.0;
        double bufferMinRange, bufferMaxRange;

        bufferMinRange = initialRollAngle - errorRange;
        bufferMaxRange = initialRollAngle + errorRange;
        if(bufferMinRange > results[2]*180/Math.PI)
        {
            errorOnRoll += 1;
        }
        else if(bufferMaxRange < results[2]*180/Math.PI)
        {
            errorOnRoll += 1;
        }
    }

    /* Detecting how many yaw angle errors from rehab */
    private void detectYawError()
    {
        // Setting the range of error
        double errorRange = 5.0;
        double bufferMinRange, bufferMaxRange;

        bufferMinRange = initialYawAngle - errorRange;
        bufferMaxRange = initialYawAngle + errorRange;

        if(bufferMinRange > results[0]*180/Math.PI)
        {
            errorOnYaw += 1;
        }
        else if(bufferMaxRange < results[0]*180/Math.PI)
        {
            errorOnYaw += 1;
        }
    }

    /* Detecting movement of user */
    private void detectChange()
    {
        // To calculate the angle move from initial position to end position
        rangeAngle = Math.abs((results[1]*180/Math.PI) - initialAngle);

        if(firstMin == 0) {
            minRepAngle = rangeAngle;
            firstMin = 1;
        }
        else if(rangeAngle > minRepAngle) // To keep track of max and min angle movement
        {
            maxRepAngle = rangeAngle;
        }
        else
        {
            minRepAngle = rangeAngle;
        }

        // Total reps divide by 2 so that it will not double calculate due to both up and down movements
        // Meaning to calculate repetitions only based on up command
        if(repSetter%2 == 0)
        {
            reps += 1;
        }
        initialAngle = results[1] * 180 / Math.PI;
    }

    /* Saving overall data to MetaData file */
    private void saveMetaData()
    {
        // Finding the total amount of errors
        totalError = errorOnRoll + errorOnYaw;
        // Getting the calendar date
        c = Calendar.getInstance();
        String formattedDate = df.format(c.getTime());

        metaData = formattedDate + "," + Integer.toString(elapsedTime) + "," + mode + "," + sides + "," + Integer.toString(reps) + "," + Double.toString(minRepAngle) + "," + Double.toString(maxRepAngle) + "," + Integer.toString(totalError) + "," + "\n";
        metaList.add(metaData);
    }

    /* Saving overall data to a list */
    private void saveMetaList(){
        // Setting up variables used for creating a file
        File internalFile;
        String fileName = "MetaData.txt";
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File dir = contextWrapper.getExternalFilesDir("MyFileStorage");
        internalFile = new File(dir, fileName);

        // writing array list of data into the text file
        try
        {
            FileOutputStream outputStream = new FileOutputStream(internalFile, true);
            for(String s: metaList)
            {
                outputStream.write(s.getBytes());

            }
            outputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /* Passing variables to another activity */
    private void switchActivity()
    {
        Intent intentBundle = new Intent(ExercisePage.this, SummaryPage.class);
        Bundle bundle = new Bundle();
        bundle.putString("Level", mode);
        bundle.putInt("Reps", reps);
        bundle.putString("Sides", sides);
        bundle.putInt("Sets", 1);
        bundle.putInt("Time", elapsedTime);
        bundle.putInt("Error", totalError);
        intentBundle.putExtras(bundle);
        startActivity(intentBundle);
    }
}
