package org.ambientlight.room;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.device.drivers.AnimateableLedDevice;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.RemoteSwtichDeviceDriver;
import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.device.led.StripePart;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.process.ProcessManager;
import org.ambientlight.room.entities.alarm.AlarmManager;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.sceneries.SceneryManager;
import org.ambientlight.room.entities.switches.SwitchManager;


/**
 * 
 * @author florian
 * 
 */
public class Room {

	public ClimateManager climateManager;

	public SwitchManager schwitchManager;

	public QeueManager qeueManager;

	public SceneryManager sceneryManager;

	public AlarmManager alarmManager;

	public CallBackManager callBackMananger;

	public EventManager eventManager;

	public ProcessManager processManager;

	private List<DeviceDriver> devices;

	private BufferedImage roomBitMap;

	public RoomConfiguration config;

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




	public List<StripePart> getAllStripePartsInRoom() {
		List<StripePart> result = new ArrayList<StripePart>();
		for (DeviceDriver currentDevice : devices) {
			if (currentDevice instanceof LedStripeDeviceDriver) {
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


	public RemoteSwtichDeviceDriver getSwitchingDevice() {
		// TODO actually we use the first found device. later a correlation
		// between the device and the switches could be possible
		for (DeviceDriver currentDevice : this.devices) {
			if (currentDevice instanceof RemoteSwtichDeviceDriver)
				return (RemoteSwtichDeviceDriver) currentDevice;
		}
		return null;
	}


	public List<AnimateableLedDevice> getLedAnimateableDevices() {
		List<AnimateableLedDevice> result = new ArrayList<AnimateableLedDevice>();
		for (DeviceDriver currentDevice : this.devices) {
			if (currentDevice instanceof AnimateableLedDevice) {
				result.add((AnimateableLedDevice) currentDevice);
			}
		}

		return result;
	}
}
