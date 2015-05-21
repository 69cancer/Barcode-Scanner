package com.dxd.barcodescanner;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class ScannerActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scanner_fragment);
    }
}