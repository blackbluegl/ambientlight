package org.ambientlight.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.eventgenerator.EventGeneratorConfiguration;
import org.ambientlight.scenery.AbstractSceneryConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("room")
public class RoomConfiguration {
	public String roomName;
	public AbstractSceneryConfiguration currentSceneryConfig;
	public int width;
	public int height;
	public Map<String,ActorConfiguration> actorConfigurations = new HashMap<String,ActorConfiguration>();
	public List<DeviceConfiguration> deviceConfigurations = new ArrayList<DeviceConfiguration>();
	public List<ProcessConfiguration> processes = new ArrayList<ProcessConfiguration>();
	public List<AbstractSceneryConfiguration> sceneries = new ArrayList<AbstractSceneryConfiguration>();
	public List<EventGeneratorConfiguration> eventGeneratorConfigurations = new ArrayList<EventGeneratorConfiguration>();
}
