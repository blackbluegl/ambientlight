package org.ambient.control.sceneryconfiguration;

import java.util.HashMap;
import java.util.Map;

import org.ambient.control.R;
import org.ambient.util.GuiUtils;
import org.ambient.views.adapter.ListIconArrayAdapter;
import org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;


public class SceneryProgramChooserActivity extends FragmentActivity {

	String lightObject = null;
	String roomServer = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle values = getIntent().getExtras();
		if (values == null)
			return;
		values.putBoolean("editAsNew", true);

		final boolean isLargeLayout = GuiUtils.isLargeLayout(this);
		setContentView(R.layout.activity_sceneries_chooser);
		ListView listView = (ListView) findViewById(R.id.listViewSceneryChooser);

		final Resources res = getResources();
		final Map<String, String> valuesMap = new HashMap<String, String>();
		final int[] iconsMap = new int[valuesMap.size()];
		iconsMap[0] = R.drawable.ic_simple_color_active;

		valuesMap.put(res.getString(R.string.program_simple_color), SimpleColorRenderingProgramConfiguration.class.getName());
		valuesMap.put(res.getString(R.string.program_tron), TronRenderingProgrammConfiguration.class.getName());


		ListIconArrayAdapter adapter = new ListIconArrayAdapter(this, valuesMap.keySet().toArray(new String[0]), iconsMap);
		listView.setAdapter(adapter);
		final SceneryProgramChooserActivity myself = this;
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView result = (TextView) view.findViewById(R.id.textViewIconArrayAdapterEntry);

				values.putString("configType", valuesMap.get(result.getText()));
				values.putString("title",result.getText().toString());

				FragmentManager fm = getSupportFragmentManager();
				SceneryConfigEditDialogFragment editSceneryConfigFragment = new SceneryConfigEditDialogFragment();

				editSceneryConfigFragment.setArguments(values);

				if (isLargeLayout) {
					// The device is using a large layout, so show the fragment as a dialog
					editSceneryConfigFragment.show(fm, null);
				} else {
					// The screen is smaller, so show the fragment in a fullscreen activity
					Intent i = new Intent(myself, SceneryConfigEditDialogHolder.class);
					i.putExtras(values);
					startActivity(i);
				}

			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sceneries_chooser, menu);
		return true;
	}

}
