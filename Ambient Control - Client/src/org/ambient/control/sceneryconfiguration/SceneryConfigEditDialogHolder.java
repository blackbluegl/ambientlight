package org.ambient.control.sceneryconfiguration;

import org.ambient.control.MainActivity;
import org.ambient.control.R;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;


public class SceneryConfigEditDialogHolder extends FragmentActivity {

	public AbstractSceneryConfigEditDialogFragment myFragment;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog_holder);

		Bundle values = getIntent().getExtras();
		this.myFragment = SceneryConfigDialogFragmentFactory.getByStringResource(values.getInt("resourceId"));
		myFragment.setArguments(values);
		setTitle(myFragment.getTitle());

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Button applyButton = (Button) findViewById(R.id.buttonDialogHolderApply);
		applyButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				myFragment.applyAction();

			}
		});
		LinearLayout content = (LinearLayout) findViewById(R.id.linearLayoutDialogContent);
		if (getSupportFragmentManager().findFragmentById(content.getId()) == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(content.getId(), myFragment);
			ft.commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dialog_holder, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_finish:

			myFragment.applyAction();

			Intent i = new Intent(this, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			finish();

			return true;

		case android.R.id.home:
			myFragment.cancelAction();
			onBackPressed();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
