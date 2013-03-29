package org.ambientlight.scenery.entities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.stripe.Stripe;

/**
 * 
 * @author florian
 * 
 */
public class Room {

	private List<DeviceDriver> devices;

	private List<LightObject> lightObjects;

	private BufferedImage roomBitMap;

	public BufferedImage getRoomBitMap() {
		return roomBitMap;
	}

	public void setRoomBitMap(BufferedImage roomBitMap) {
		this.roomBitMap = roomBitMap;
	}

	public List<DeviceDriver> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceDriver> devices) {
		this.devices = devices;
	}

	public List<LightObject> getLightObjectsInRoom() {
		return lightObjects;
	}
	
	public LightObject getLightObjectByName(String name){
		for (LightObject current : this.lightObjects){
			if(name.equals(current.getConfiguration().lightObjectName)){
				return current;
			}
		}
		return null;
	}

	public void setLightObjectsInRoom(List<LightObject> lightObjectsInRoom) {
		this.lightObjects = lightObjectsInRoom;
	}

	public List<StripePart> getAllStripePartsInRoom() {
		List<StripePart> result = new ArrayList<StripePart>();
		for (DeviceDriver currentDevice : devices) {
			for (Stripe currentStripe : currentDevice.getAllStripes()) {
				result.addAll(currentStripe.getStripeParts());
			}
		}
		return result;
	}

}
