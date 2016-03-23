package org.chromium.customtabsdemos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class returnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return);

        Intent intent = getIntent();
        Uri data = intent.getData();


    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView a = (TextView) findViewById(R.id.textview);
        a.setText("Thanks for using paypal, you just finish the approval step. You are ready to exec the payment.");
    }
}
