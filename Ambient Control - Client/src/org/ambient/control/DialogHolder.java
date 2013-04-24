package org.ambient.control;

import org.ambient.control.home.ProgrammEditFragmentFactory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.widget.RelativeLayout;



public class DialogHolder extends FragmentActivity {
	public Fragment myFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 Bundle values = getIntent().getExtras();
		Fragment myFragment = ProgrammEditFragmentFactory.getByMapperName(values.getString("dialog"));
		myFragment.setArguments(values);
		setContentView(R.layout.activity_dialog_holder);
		RelativeLayout content = (RelativeLayout) findViewById(R.id.layoutDialogContent);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(content.getId(), myFragment);
		ft.commit();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dialog_holder, menu);
		return true;
	}

}
