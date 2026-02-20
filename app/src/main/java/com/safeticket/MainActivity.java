package com.safeticket;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.safeticket.ui.FeedFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for the activity

        if (savedInstanceState == null) { // Check if the activity is being created for the first time
            getSupportFragmentManager().beginTransaction() // Start a fragment transaction
                    .replace(R.id.main_container, new FeedFragment()) // Replace the container with the FeedFragment
                    .commit(); // Do the transaction
        }
    }
}