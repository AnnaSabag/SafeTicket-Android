package com.safeticket;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.safeticket.ui.FeedFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load FeedFragment immediately when app starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new FeedFragment())
                    .commit();
        }
    }
}