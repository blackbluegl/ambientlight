package org.ambient.control.programs;

import org.ambient.control.MainActivity;
import org.ambient.control.R;
import org.ambient.control.rest.RestClient;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import de.devmil.common.ui.color.ColorSelectorView;

public class ProgramEditorActivity extends Activity {

    private String roomServer;
	private String lightObject;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_editor);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle values = getIntent().getExtras();
        if (values == null) {
          return;
        }
        lightObject = values.getString("lightObject");
        roomServer = values.getString("roomServer");

        int color =0;
        try {
			RoomConfiguration room = RestClient.getRoom(roomServer);
			RoomItemConfiguration current =	room.getRoomItemConfigurationByName(lightObject);
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) 
					current.getSceneryConfigurationBySceneryName(room.currentScenery);
			color=	Color.rgb(config.getR(), config.getG(), config.getB());
		} catch (Exception e) {
			e.printStackTrace();
		}
        
       LinearLayout contentView = (LinearLayout)findViewById(R.id.layoutProgramContent);     
       
       LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       LinearLayout content = (LinearLayout) inflater.inflate(R.layout.layout_color_picker, null);
       contentView.addView(content);
       final ColorSelectorView colorPicker = (ColorSelectorView)content.findViewById(R.id.colorSelectorView);
       colorPicker.setColor(color);
       
        Button okButton = (Button) findViewById(R.id.buttonProgramEditorApply);
        okButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int color = colorPicker.getColor();
				SimpleColorRenderingProgramConfiguration sc = new SimpleColorRenderingProgramConfiguration();
				sc.setR(Color.red(color));
				sc.setG(Color.green(color));
				sc.setB(Color.blue(color));
				RestClient.setProgramForLightObject(roomServer,lightObject,sc);
			}
		});
    
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_program_editor, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case android.R.id.home:
            // This is called when the Home (Up) button is pressed
            // in the Action Bar.
            Intent parentActivityIntent = new Intent(this, MainActivity.class);
            parentActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
