package org.ambientlight.room;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.device.drivers.AnimateableLedDevice;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.device.led.StripePart;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.process.ProcessManager;
import org.ambientlight.room.entities.alarm.AlarmManager;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.lightobject.LightObjectManager;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitchManager;
import org.ambientlight.room.entities.sceneries.SceneryManager;
import org.ambientlight.room.entities.switches.SwitchManager;


/**
 * 
 * @author florian
 * 
 */
public class Room {

	public LightObjectManager lightObjectManager;

	public ClimateManager climateManager;

	public SwitchManager schwitchManager;

	public RemoteSwitchManager remoteSwitchManager;

	public QeueManager qeueManager;

	public SceneryManager sceneryManager;

	public AlarmManager alarmManager;

	public CallBackManager callBackMananger;

	public EventManager eventManager;

	public ProcessManager processManager;

	private List<DeviceDriver> devices;


	public RoomConfiguration config;




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
