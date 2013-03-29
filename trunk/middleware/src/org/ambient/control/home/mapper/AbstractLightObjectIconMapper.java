package org.ambient.control.home.mapper;

import org.ambient.control.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class AbstractLightObjectIconMapper{

	private boolean powerState  = false;
	private String serverName;
	private View lightObject;
	private String lightObjectName;

	
	public AbstractLightObjectIconMapper(View lightObject, String lightObjectName, String serverName, boolean powerState) {
		this.lightObject=lightObject;

		TextView labelView = (TextView) lightObject.findViewById(R.id.textLightObjectName);
		labelView.setText(lightObjectName);
		
		this.lightObjectName=lightObjectName;
		
		this.serverName=serverName;
		
		ImageView icon = (ImageView) lightObject.findViewById(R.id.imageViewLightObject);
		if(powerState==false){
			icon.setImageResource(this.getDisabledIcon());
			this.powerState=false;
		}
		else{
			icon.setImageResource(this.getActiveIcon());
			this.powerState=true;
		}
	}
	
	
	public String getServerName() {
		return serverName;
	}

	
	public String getLightObjectName(){
		return lightObjectName;
	}

	
	public View getLightObjectView() {
		return lightObject;
	}


	public void setPowerState(boolean isActive){
		this.powerState=isActive;
		ImageView icon = (ImageView) lightObject.findViewById(R.id.imageViewLightObject);
		if(isActive){
			icon.setImageResource(this.getActiveIcon());
		}
		else{
			icon.setImageResource(this.getDisabledIcon());
		}
	}
	
	
	public boolean getPowerState(){
		return powerState;
	}
	
	
	protected abstract int getActiveIcon();
	protected abstract int getDisabledIcon();
}
