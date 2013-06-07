package org.ambientlight.room.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ambientlight.scenery.EntitiyConfiguration;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class RoomItemConfiguration {

	public String name;

	public Map<String, EntitiyConfiguration> sceneryConfigurationBySzeneryName = new HashMap<String, EntitiyConfiguration>();

	public EntitiyConfiguration getSceneryConfigurationBySceneryName(String sceneryName) {
		return sceneryConfigurationBySzeneryName.get(sceneryName);
	}

	@JsonIgnore
	public Set<String> getSupportedSceneries(){
		return sceneryConfigurationBySzeneryName.keySet();
	}
}
