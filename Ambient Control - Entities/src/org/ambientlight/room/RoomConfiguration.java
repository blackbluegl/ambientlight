package org.ambientlight.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.trigger.EventTriggerConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.scenery.AbstractSceneryConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("room")
public class RoomConfiguration {
	public String roomName;
	public String currentScenery;
	public int width;
	public int height;
	public Map<String,ActorConfiguration> actorConfigurations = new HashMap<String,ActorConfiguration>();
	public Map<String,EventTriggerConfiguration> eventTriggerConfigurations = new HashMap<String, EventTriggerConfiguration>();
	public List<DeviceConfiguration> deviceConfigurations = new ArrayList<DeviceConfiguration>();
	public List<ProcessConfiguration> processes = new ArrayList<ProcessConfiguration>();
	public List<AbstractSceneryConfiguration> sceneries = new ArrayList<AbstractSceneryConfiguration>();
}
