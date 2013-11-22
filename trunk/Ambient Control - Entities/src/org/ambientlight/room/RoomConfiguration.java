package org.ambientlight.room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.eventgenerator.EventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;
import org.ambientlight.room.sensor.SensorConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("room")
public class RoomConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public String roomName;
	public int width;
	public int height;

	public ClimateConfiguration climate;

	Map<String, SensorConfiguration> sensors;
	public Map<String, ActorConfiguration> actorConfigurations = new HashMap<String, ActorConfiguration>();
	public List<DeviceConfiguration> deviceConfigurations = new ArrayList<DeviceConfiguration>();
	public List<EventProcessConfiguration> processes = new ArrayList<EventProcessConfiguration>();
	public Map<String, EventGeneratorConfiguration> eventGeneratorConfigurations = new HashMap<String, EventGeneratorConfiguration>();
	public SceneryEventGeneratorConfiguration sceneryEventGenerator;


	public Map<String, SensorConfiguration> getSensors() {
		return sensors;
	}


	public Map<String, ISwitchableRoomItem> getUserRoomItems() {
		Map<String, ISwitchableRoomItem> result = new HashMap<String, ISwitchableRoomItem>();
		for (ActorConfiguration actorConfig : actorConfigurations.values()) {
			if (actorConfig instanceof ISwitchableRoomItem) {
				result.put(actorConfig.getName(), actorConfig);
			}
		}

		for (EventGeneratorConfiguration eventGeneratorConfiguration : eventGeneratorConfigurations.values()) {
			if (eventGeneratorConfiguration instanceof ISwitchableRoomItem) {
				result.put(((ISwitchableRoomItem) eventGeneratorConfiguration).getName(), (ISwitchableRoomItem) eventGeneratorConfiguration);
			}
		}

		return result;
	}


	public Map<String, SwitchEventGeneratorConfiguration> getSwitchGenerators() {
		Map<String, SwitchEventGeneratorConfiguration> result = new HashMap<String, SwitchEventGeneratorConfiguration>();

		for (EventGeneratorConfiguration eventGeneratorConfiguration : eventGeneratorConfigurations.values()) {
			if (eventGeneratorConfiguration instanceof SwitchEventGeneratorConfiguration) {
				result.put(((SwitchEventGeneratorConfiguration) eventGeneratorConfiguration).name,
						(SwitchEventGeneratorConfiguration) eventGeneratorConfiguration);
			}
		}
		return result;
	}


	public SceneryEventGeneratorConfiguration getSceneryEventGeneratorConfiguration() {
		return sceneryEventGenerator;
	}

}
