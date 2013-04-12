package org.ambient.control.programs;

import java.util.HashMap;
import java.util.Map;

import org.ambient.control.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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

public class ProgramChooserActivity extends Activity {

    String lightObject = null;
    String roomServer = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle values = getIntent().getExtras();
        if (values == null) {
          return;
        }
        lightObject = values.getString("lightObject");
        roomServer = values.getString("roomServer");
        
        setContentView(R.layout.activity_sceneries_chooser);
        ListView listView = (ListView) findViewById(R.id.listViewSceneryChooser);
        
        final Resources res = getResources();
        final Map<String,Integer> valuesMap = new HashMap<String,Integer>();
        valuesMap.put(res.getString(R.string.program_simple_color), R.string.program_simple_color);

        ListIconArrayAdapter adapter = new ListIconArrayAdapter(this, valuesMap.keySet().toArray(new String[0]));
        listView.setAdapter(adapter);
        final ProgramChooserActivity myself = this;
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
				    int position, long id) {
			TextView result = (TextView) view.findViewById(R.id.textViewSceneryChooserEntryLabel);	
			int resourceId = valuesMap.get(result.getText());
			
		   	Intent i = new Intent(myself, ProgramEditorActivity.class);
        	i.putExtra("roomServer", roomServer);
        	i.putExtra("lightObject", lightObject); 
        	i.putExtra("programResourceId", resourceId);
        	startActivity(i);
			
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
    	    LayoutInflater inflater = (LayoutInflater) context
    	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
