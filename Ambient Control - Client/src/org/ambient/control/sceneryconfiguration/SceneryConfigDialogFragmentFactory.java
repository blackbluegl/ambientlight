package org.ambient.control.sceneryconfiguration;

import org.ambient.control.R;

public class SceneryConfigDialogFragmentFactory {

	public static AbstractSceneryConfigEditDialogFragment getByStringResource(int resourceId){
		if(R.string.program_tron == resourceId){
			return new TronEditDialog();	
		}
		if(R.string.program_simple_color == resourceId){
			return new SimpleColorEditDialog();
		}
		return null;
	}
}
