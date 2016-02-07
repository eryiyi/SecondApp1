package com.example.secondapp.activity;

import android.content.Context;
import android.content.res.AssetManager;
import com.example.secondapp.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import com.example.secondapp.base.BaseActivity;
import com.example.secondapp.view.MyTextView2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutSelf extends BaseActivity implements View.OnClickListener{
	MyTextView2 view;

	@Override
	protected void onCreate(Bundle arg0) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(arg0);
		setContentView(R.layout.aboutmyself);

		this.findViewById(R.id.back).setOnClickListener(this);
		view = (MyTextView2) findViewById(R.id.view);
		view.setText(getAssetsString(this,"2.txt"));
		this.findViewById(R.id.back).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.back:
				finish();
				break;
		}
	}
	public String getAssetsString(Context context,String fileName){
		StringBuffer sb = new StringBuffer();
		try {
			AssetManager am = context.getAssets();
			InputStream in = am.open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = reader.readLine())!=null){
				line += ("\n");
				sb.append(line);
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
