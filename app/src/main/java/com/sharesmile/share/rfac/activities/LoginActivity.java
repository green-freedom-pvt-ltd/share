package com.sharesmile.share.rfac.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.sharesmile.share.R;

/**
 * Created by apurvgandhwani on 4/5/2016.
 */
public class LoginActivity extends AppCompatActivity {
    TextView tv_skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);
        tv_skip = (TextView) findViewById(R.id.tv_welcome_skip);
        tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
