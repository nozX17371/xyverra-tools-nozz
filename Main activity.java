package com.xyverra.app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("Xyverra APK berhasil di-build! 🚀");
        tv.setTextSize(20);
        tv.setPadding(50, 200, 50, 50);
        setContentView(tv);
    }
}