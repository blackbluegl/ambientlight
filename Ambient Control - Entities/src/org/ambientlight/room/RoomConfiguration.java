package org.ambientlight.room;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.process.EventProcessConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.eventgenerator.EventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;
import org.ambientlight.scenery.AbstractSceneryConfiguration;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("room")
public class RoomConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public String roomName;
	public int width;
	public int height;

	public ClimateConfiguration climate;

	public Map<String, ActorConfiguration> actorConfigurations = new HashMap<String, ActorConfiguration>();
	public List<DeviceConfiguration> deviceConfigurations = new ArrayList<DeviceConfiguration>();
	public List<EventProcessConfiguration> processes = new ArrayList<EventProcessConfiguration>();
	public Map<String, EventGeneratorConfiguration> eventGeneratorConfigurations = new HashMap<String, EventGeneratorConfiguration>();


	public Map<String, IUserRoomItem> getUserRoomItems() {
		Map<String, IUserRoomItem> result = new HashMap<String, IUserRoomItem>();
		for (ActorConfiguration actorConfig : actorConfigurations.values()) {
			if (actorConfig instanceof IUserRoomItem) {
				result.put(actorConfig.getName(), actorConfig);
			}
		}

		for (EventGeneratorConfiguration eventGeneratorConfiguration : eventGeneratorConfigurations.values()) {
			if (eventGeneratorConfiguration instanceof IUserRoomItem) {
				result.put(((IUserRoomItem) eventGeneratorConfiguration).getName(), (IUserRoomItem) eventGeneratorConfiguration);
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


	public Map<String, SceneryEventGeneratorConfiguration> getSceneryEventGenerator() {
		Map<String, SceneryEventGeneratorConfiguration> result = new HashMap<String, SceneryEventGeneratorConfiguration>();

		for (EventGeneratorConfiguration eventGeneratorConfiguration : eventGeneratorConfigurations.values()) {
			if (eventGeneratorConfiguration instanceof SceneryEventGeneratorConfiguration) {
				result.put(((SceneryEventGeneratorConfiguration) eventGeneratorConfiguration).name,
						(SceneryEventGeneratorConfiguration) eventGeneratorConfiguration);
			}
		}
		return result;
	}


	public List<AbstractSceneryConfiguration> getSceneries() {
		ArrayList<AbstractSceneryConfiguration> result = new ArrayList<AbstractSceneryConfiguration>();
		for (SceneryEventGeneratorConfiguration current : getSceneryEventGenerator().values()) {
			result.addAll(current.sceneries);
		}
		return result;
	}


	@JsonIgnore
	public AbstractSceneryConfiguration getCurrentScenery() {

		for (SceneryEventGeneratorConfiguration eventGen : getSceneryEventGenerator().values())
			// we assume that there is only one per room
			return eventGen.currentScenery;
		return null;
	}

}
