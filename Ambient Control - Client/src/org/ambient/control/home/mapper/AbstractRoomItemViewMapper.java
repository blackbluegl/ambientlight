package org.ambient.control.home.mapper;

import org.ambient.control.R;
import org.ambientlight.room.entities.features.EntityId;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public abstract class AbstractRoomItemViewMapper {

	private boolean eventListenerDisabled = false;

	private boolean powerState = false;
	private final View itemView;
	private final EntityId entityId;
	private final int resourceId;


	public AbstractRoomItemViewMapper(View itemView, EntityId entityId, int resourceId, boolean powerState) {
		this.itemView = itemView;

		TextView labelView = (TextView) itemView.findViewById(R.id.textLightObjectName);
		labelView.setText(entityId.id);

		this.entityId = entityId;

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
		return entityId.id;
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
