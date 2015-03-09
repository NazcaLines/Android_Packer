package com.nazcalines.shelldemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ShellActivity extends Activity{
	
	public static final String TAG = "ShellActivity";
	
	Button mLoginButton;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "=======ShellActivity onCreate=======");
		int layoutID = getResources().getIdentifier("activity_shell",
				"layout", getPackageName());
		setContentView(layoutID);
		
		int btnid = getResources()
				.getIdentifier("btn_start", "id",
				getPackageName());
		
		mLoginButton = (Button) findViewById(btnid);
		mLoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startOriginActivity();
			}
		});

	}
	
	protected void startOriginActivity() {
        
        int resID = getResources().getIdentifier("originapk_activity", "string", getPackageName()); 
        String originMainActivity = getResources().getString(resID);
 
        Log.i(TAG, "startOriginActivity--"+originMainActivity);
        try {
        	Intent i = new Intent(this, getApplication().getClassLoader()
                    .loadClass(originMainActivity));
            startActivity(i);
            return;
        }
        catch (Exception e) {
            Toast.makeText(this, "problem happens",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "start Oringin Activity Error", e);
        }
    }

}
