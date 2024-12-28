package com.sk.revisit.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sk.revisit.R;

class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        LinearLayout linearLayout1 = new LinearLayout(this);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);

        setContentView(linearLayout1);

        TextView textView1 = new TextView(this);
        textView1.setText(R.string.title_log);

        linearLayout1.addView(textView1);
    }
}