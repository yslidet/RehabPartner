package com.fyp.lun.rehabpartner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class SummaryPage extends AppCompatActivity {

    /* Variables Initialization */
    private Button homeBtn;
    private TextView modeTV, sideTV, repsTV, setsTV, timeTV, incorrectTV;
    private boolean hasLevel = false, hasReps = false, hasSides = false, hasTime = false, hasSets = false, hasError = false;
    String mode = "", sides = "";
    int repsNeeded = 0, time = 0, sets = 0, error = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_page);

        /* Initialize layout */
        homeBtn = (Button) findViewById(R.id.homeBtn);
        modeTV = (TextView) findViewById(R.id.levelTV);
        sideTV = (TextView) findViewById(R.id.sideTV);
        repsTV = (TextView) findViewById(R.id.repsTV);
        setsTV = (TextView) findViewById(R.id.setsTV);
        timeTV = (TextView) findViewById(R.id.timeTV);
        incorrectTV = (TextView) findViewById(R.id.incorrectTV);


       /* Getting previous activity's data */
        Intent intentExtras = getIntent();
        Bundle extrasBundle = intentExtras.getExtras();
        if(!extrasBundle.isEmpty())
        {
            hasLevel = extrasBundle.containsKey("Level");
            hasReps = extrasBundle.containsKey("Reps");
            hasSides = extrasBundle.containsKey("Sides");
            hasSets = extrasBundle.containsKey("Sets");
            hasTime = extrasBundle.containsKey("Time");
            hasError = extrasBundle.containsKey("Error");

        }
        if(hasLevel)
        {
            mode = extrasBundle.getString("Level");
            modeTV.setText(" " + mode);
        }
        if(hasReps)
        {
            repsNeeded = extrasBundle.getInt("Reps");
            repsTV.setText(" " + repsNeeded);
        }
        if(hasSides)
        {
            sides = extrasBundle.getString("Sides");
            sideTV.setText(" " + sides + " leg");

        }
        if(hasSets)
        {
            sets = extrasBundle.getInt("Sets");
            setsTV.setText(" " + sets);
        }
        if(hasTime)
        {
            time = extrasBundle.getInt("Time");
            timeTV.setText("    " + time + "secs");
        }
        if(hasError)
        {
            error = extrasBundle.getInt("Error");
            incorrectTV.setText("" + error);
        }

        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(SummaryPage.this, MainActivity.class));
            }
        });
    }
}
