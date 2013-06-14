package com.example.lo52_project;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 
 * Launch activity 
 * This is a simple activity that contains the main menu.
 * Choice between simple and complex world.
 * @author Lois Aubree & Lucie Boutou
 *
 */
public class StartActivity extends Activity implements OnClickListener {
	
	private Button simple_button = null;
	private Button complexe_button = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_layout);
		
		simple_button = (Button)findViewById(R.id.SimpleWorldButton);
		complexe_button = (Button)findViewById(R.id.CompleWorldButton);
		
		simple_button.setOnClickListener(this);
		complexe_button.setOnClickListener(this);
		
	}
	/**
	 * Menu bouton on click event
	 * Run the activity selected as MAIN Activity 
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.SimpleWorldButton:
				Intent intent_simp = new Intent(getApplicationContext(),SimpleWorldActivity.class);
				intent_simp.setAction(Intent.ACTION_MAIN);
				startActivity(intent_simp);
				break;
			case R.id.CompleWorldButton:
				Intent intent_comp = new Intent(getApplicationContext(),ComplexeWorldActivity.class);
				intent_comp.setAction(Intent.ACTION_MAIN);
				startActivity(intent_comp);
				break;
			default:
				break;
		}
	}

}
