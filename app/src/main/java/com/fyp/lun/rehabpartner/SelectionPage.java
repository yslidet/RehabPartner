package com.fyp.lun.rehabpartner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SelectionPage extends AppCompatActivity {

    private Button btnBeginner, btnIntermediate, btnAdvance, btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_page);

         /* Button Initialization */
        btnBeginner = (Button) findViewById(R.id.beginnerBtn);
        btnIntermediate = (Button) findViewById(R.id.interBtn);
        btnAdvance = (Button) findViewById(R.id.advanceBtn);
        btnBack = (Button) findViewById(R.id.backBtn);

        /* Button Listener */
        btnBeginner.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentBundle = new Intent(SelectionPage.this, SeatedKneeExtension.class);
                Bundle bundle = new Bundle();
                bundle.putString("Level", "Beginner");
                bundle.putInt("Reps", 3);
                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
            }
        });

        btnIntermediate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentBundle = new Intent(SelectionPage.this, SeatedKneeExtension.class);
                Bundle bundle = new Bundle();
                bundle.putString("Level", "Intermediate");
                bundle.putInt("Reps", 5);
                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
            }
        });

        btnAdvance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intentBundle = new Intent(SelectionPage.this, SeatedKneeExtension.class);
                Bundle bundle = new Bundle();
                bundle.putString("Level", "Advance");
                bundle.putInt("Reps", 10);
                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(SelectionPage.this, MainActivity.class));
            }
        });
    }
}
