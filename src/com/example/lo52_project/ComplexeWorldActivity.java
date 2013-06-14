package com.example.lo52_project;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Beginning of implementation of the complex world activity
 * @author Lois Aubree & Lucie Boutou
 *
 */
public class ComplexeWorldActivity extends WorldActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		mMainLayout = (FrameLayout)findViewById(R.id.main_layout);
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.button_left:
			break;
		case R.id.button_right:
			break;
		case R.id.button_forward:
	
			break;
		case R.id.button_backward:
			break;
		default:
			break;
		}
		return true;
	}
	@Override
	public void onClick(View v) {
		
	}

}
