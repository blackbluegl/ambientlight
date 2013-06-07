package org.ambientlight.scenery.entities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.SwtichDeviceDriver;
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
			if(name.equals(current.getConfiguration().name)){
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
			if(currentDevice instanceof LedStripeDeviceDriver){
				LedStripeDeviceDriver currentLedStripeDevice = (LedStripeDeviceDriver) currentDevice;
				for (Stripe currentStripe : currentLedStripeDevice.getAllStripes()) {
					result.addAll(currentStripe.getStripeParts());
				}
			}
		}
		return result;
	}

	
	public SwtichDeviceDriver getSwitchingDevice() {
		// TODO actually we use the first found device. later a correlation between the device and the switches could be possible
		for(DeviceDriver currentDevice : this.devices){
			if(currentDevice instanceof SwtichDeviceDriver){
				return (SwtichDeviceDriver) currentDevice;
			}
		}
		
		return null;
	}


	public List<LedStripeDeviceDriver> getLedStripeDevices() {
		List<LedStripeDeviceDriver> result = new ArrayList<LedStripeDeviceDriver>();
		for(DeviceDriver currentDevice : this.devices){
			if(currentDevice instanceof LedStripeDeviceDriver){
				result.add((LedStripeDeviceDriver)currentDevice);
			}
		}
		
		return result;
	}


}
