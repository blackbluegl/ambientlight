package org.ambientlight.room;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.device.drivers.AnimateableLedDevice;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.SwtichDeviceDriver;
import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.process.entities.Process;
import org.ambientlight.process.eventmanager.EventManager;
import org.ambientlight.room.entities.EventGenerator;
import org.ambientlight.room.entities.EventSensor;
import org.ambientlight.room.entities.LightObject;
import org.ambientlight.room.entities.Sensor;


/**
 * 
 * @author florian
 * 
 */
public class Room {

	public CallBackManager callBackMananger;

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


	public List<LedPoint> getAllLedPointsInRoom() {
		List<LedPoint> result = new ArrayList<LedPoint>();
		for (DeviceDriver currentDevice : devices) {
			if (currentDevice instanceof LedPointDeviceDriver) {
				LedPointDeviceDriver currentLedPointDevice = (LedPointDeviceDriver) currentDevice;
				result.addAll(currentLedPointDevice.getLedPoints());
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


	public List<AnimateableLedDevice> getLedAnimateableDevices() {
		List<AnimateableLedDevice> result = new ArrayList<AnimateableLedDevice>();
		for(DeviceDriver currentDevice : this.devices){
			if (currentDevice instanceof AnimateableLedDevice) {
				result.add((AnimateableLedDevice) currentDevice);
			}
		}

		return result;
	}


	public EventSensor getEventSensorById(String name) {
		EventGenerator possibleGenerator = this.eventGenerators.get(name);
		return (possibleGenerator instanceof EventSensor ? (EventSensor) possibleGenerator : null);
	}
}
