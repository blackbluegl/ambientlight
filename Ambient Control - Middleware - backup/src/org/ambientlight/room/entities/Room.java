package org.ambientlight.room.entities;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.SwtichDeviceDriver;
import org.ambientlight.device.stripe.Stripe;
import org.ambientlight.process.entities.Process;
import org.ambientlight.process.events.EventManager;
import org.ambientlight.process.events.generator.EventGenerator;
import org.ambientlight.room.RoomConfiguration;

/**
 * 
 * @author florian
 * 
 */
public class Room {

	public EventManager eventManager;

	public Map<String, Sensor> sensors;

	public Map<String, EventGenerator> eventGenerators;

	private List<DeviceDriver> devices;

	private List<LightObject> lightObjects;

	private BufferedImage roomBitMap;

	public RoomConfiguration config;

	public List<Process> processes;

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
			if (name.equals(current.configuration.getName()))
				return current;
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
			if(currentDevice instanceof SwtichDeviceDriver)
				return (SwtichDeviceDriver) currentDevice;
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
