package org.ambient.control.sceneryconfiguration;

import java.util.HashMap;
import java.util.Map;

import org.ambient.control.R;
import org.ambient.util.GuiUtils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SceneryProgramChooserActivity extends FragmentActivity {

	String lightObject = null;
	String roomServer = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle values = getIntent().getExtras();
		if (values == null) {
			return;
		}
		values.putBoolean("editAsNew", true);
		
		final boolean isLargeLayout = GuiUtils.isLargeLayout(this);
		// lightObject = values.getString("lightObject");
		// roomServer = values.getString("roomServer");
		//
		setContentView(R.layout.activity_sceneries_chooser);
		ListView listView = (ListView) findViewById(R.id.listViewSceneryChooser);

		final Resources res = getResources();
		final Map<String, Integer> valuesMap = new HashMap<String, Integer>();

		valuesMap.put(res.getString(R.string.program_simple_color), R.string.program_simple_color);
		valuesMap.put(res.getString(R.string.program_tron), R.string.program_tron);

		ListIconArrayAdapter adapter = new ListIconArrayAdapter(this, valuesMap.keySet().toArray(new String[0]));
		listView.setAdapter(adapter);
		final SceneryProgramChooserActivity myself = this;
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView result = (TextView) view.findViewById(R.id.textViewSceneryChooserEntryLabel);
				int resourceId = valuesMap.get(result.getText());

				FragmentManager fm = getSupportFragmentManager();
				AbstractSceneryConfigEditDialogFragment editSceneryConfigFragment = SceneryConfigDialogFragmentFactory
						.getByStringResource(resourceId);

				editSceneryConfigFragment.setArguments(values);

				if (isLargeLayout) {
					// The device is using a large layout, so show the fragment
					// as a
					// dialog
					editSceneryConfigFragment.show(fm, null);
				} else {
					// The screen is smaller, so show the fragment in a
					// fullscreen
					// activity
					Intent i = new Intent(myself, SceneryConfigEditDialogHolder.class);
					i.putExtras(values);
					i.putExtra("resourceId", resourceId);
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

	public class ListIconArrayAdapter extends ArrayAdapter<String> {

		private final Context context;
		private final String[] values;


		public ListIconArrayAdapter(Context context, String[] values) {
			super(context, R.layout.layout_programs_chooser_entry, values);
			this.context = context;
			this.values = values;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.layout_programs_chooser_entry, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.textViewSceneryChooserEntryLabel);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.imageViewSceneryChooserEntryIcon);
			textView.setText(values[position]);

			String s = values[position];
			Resources res = getResources();
			if (s.startsWith(res.getString(R.string.program_simple_color))) {
				imageView.setImageResource(R.drawable.ic_simple_color_active);
			}

			return rowView;
		}
	}

}
