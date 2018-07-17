package com.fyp.lun.rehabpartner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SeatedKneeExtension extends AppCompatActivity {

    /* Variables initialiation */
    private Button startBtn, backBtn, togBtn;
    private String mode = "", sides = "Right";
    private int reps = 0;
    private boolean hasLevel = false;
    private boolean hasReps = false;
    StringBuffer results = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seated_knee_extension);

        /* Getting the level and reps from previous activity */
        Intent intentExtras = getIntent();
        Bundle extrasBundle = intentExtras.getExtras();
        if(!extrasBundle.isEmpty())
        {
            hasLevel = extrasBundle.containsKey("Level");
            hasReps = extrasBundle.containsKey("Reps");
        }
        if(hasLevel)
        {
            mode = extrasBundle.getString("Level");
        }
        if(hasReps)
        {
            reps = extrasBundle.getInt("Reps");
        }

        /* Button Initialization */
        startBtn = (Button) findViewById(R.id.startBtn);
        backBtn = (Button) findViewById(R.id.backBtn);
        togBtn = (ToggleButton) findViewById(R.id.togBtn);

        /* Button listener */
        startBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                results.append(togBtn.getText());
                sides = results.toString();
                Intent intentBundle = new Intent(SeatedKneeExtension.this, ExercisePage.class);
                Bundle bundle = new Bundle();
                bundle.putString("Level", mode);
                bundle.putInt("Reps", reps);
                bundle.putString("Sides", sides);
                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(SeatedKneeExtension.this, SelectionPage.class));
            }
        });

        togBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            }

        });

    }
}
