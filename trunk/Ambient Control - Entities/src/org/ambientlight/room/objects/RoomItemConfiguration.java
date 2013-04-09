package org.ambientlight.room.objects;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.scenery.SceneryConfiguration;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class  RoomItemConfiguration {
	
	public String name;
	
	public SceneryConfiguration currentSceneryConfiguration;
	
	public List<SceneryConfiguration> sceneryConfigurationBySzeneryName = new ArrayList<SceneryConfiguration>();

	public SceneryConfiguration getSceneryConfigurationBySceneryName(String sceneryName){
		for(SceneryConfiguration current : this.sceneryConfigurationBySzeneryName){
			if(sceneryName.equals(current.sceneryName)){
				return current;
			}
		}
		return null;
	}
}
