package org.ambient.control.home.mapper;

import org.ambient.control.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public abstract class AbstractRoomItemViewMapper {

	private boolean eventListenerDisabled = false;

	private boolean powerState = false;
	private final View itemView;
	private final String itemName;
	private final int resourceId;


	public AbstractRoomItemViewMapper(View itemView, String itemName, int resourceId, boolean powerState) {
		this.itemView = itemView;

		TextView labelView = (TextView) itemView.findViewById(R.id.textLightObjectName);
		labelView.setText(itemName);

		this.itemName = itemName;

		this.resourceId = resourceId;

		ImageView icon = (ImageView) itemView.findViewById(R.id.imageViewLightObject);
		if (powerState == false) {
			icon.setImageResource(this.getDisabledIcon());
			this.powerState = false;
		} else {
			icon.setImageResource(this.getActiveIcon());
			this.powerState = true;
		}
	}


	public int getResourceId() {
		return resourceId;
	}


	public String getItemName() {
		return itemName;
	}


	public View getLightObjectView() {
		return itemView;
	}


	public boolean isEventListenerDisabled() {
		return eventListenerDisabled;
	}


	public void setEventListenerDisabled(boolean eventListenerDisabled) {
		this.eventListenerDisabled = eventListenerDisabled;
	}


	public void setPowerState(boolean isActive) {
		this.powerState = isActive;
		ImageView icon = (ImageView) itemView.findViewById(R.id.imageViewLightObject);
		if (isActive) {
			icon.setImageResource(this.getActiveIcon());
		} else {
			icon.setImageResource(this.getDisabledIcon());
		}
	}


	public boolean getPowerState() {
		return powerState;
	}


	protected abstract int getActiveIcon();


	protected abstract int getDisabledIcon();
}
