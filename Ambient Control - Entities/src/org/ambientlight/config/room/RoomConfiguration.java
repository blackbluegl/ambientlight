package org.ambientlight.config.room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.features.actor.Switchable;
import org.ambientlight.config.process.ProcessManagerConfiguration;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectConfiguration;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitchManagerConfiguration;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("room")
public class RoomConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public String roomName;
	public int width;
	public int height;

	public ClimateManagerConfiguration climateManager;
	public SwitchManagerConfiguration switchesManager;
	public RemoteSwitchManagerConfiguration remoteSwitchesManager;
	public AlarmManagerConfiguration alarmManager;
	public SceneryManagerConfiguration sceneriesManager;
	public ProcessManagerConfiguration processManager;

	public Map<String, LightObjectConfiguration> lightObjectConfigurations = new HashMap<String, LightObjectConfiguration>();

	public List<DeviceConfiguration> deviceConfigurations = new ArrayList<DeviceConfiguration>();


	public Map<String, Switchable> getSwitchableActors() {
		Map<String, Switchable> result = new HashMap<String, Switchable>();

		result.putAll(lightObjectConfigurations);

		result.putAll(switchesManager.switches);

		result.putAll(remoteSwitchesManager.remoteSwitches);

		return result;
	}
}
