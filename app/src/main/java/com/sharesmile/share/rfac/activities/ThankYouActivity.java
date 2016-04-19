package com.sharesmile.share.rfac.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.sharesmile.share.R;

/**
 * Created by apurvgandhwani on 4/9/2016.
 */
public class ThankYouActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thank_you);


        RelativeLayout layout_thank_you = (RelativeLayout) findViewById(R.id.thank_you_layout);
        layout_thank_you.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThankYouActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
