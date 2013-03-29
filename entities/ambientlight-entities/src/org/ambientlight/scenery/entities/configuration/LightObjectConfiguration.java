package org.ambientlight.scenery.entities.configuration;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.scenery.rendering.programms.configuration.RenderingProgrammConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("lightObject")
public class LightObjectConfiguration {
	

	
	public RenderingProgrammConfiguration currentRenderingProgrammConfiguration;
	
	public List<RenderingProgrammConfiguration> renderingProgrammConfigurationBySzeneryName = new ArrayList<RenderingProgrammConfiguration>();
	
	public int xOffsetInRoom;
	
	public int yOffsetInRoom;
	
	public int height;
	
	public int width;
	
	public int layerNumber;
	
	public String lightObjectName;

	public RenderingProgrammConfiguration getRenderingProgrammConfigurationBySceneryName(String sceneryName){
		for(RenderingProgrammConfiguration current : this.renderingProgrammConfigurationBySzeneryName){
			if(sceneryName.equals(current.sceneryName)){
				return current;
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + layerNumber;
		result = prime * result
				+ ((lightObjectName == null) ? 0 : lightObjectName.hashCode());
		result = prime * result + width;
		result = prime * result + xOffsetInRoom;
		result = prime * result + yOffsetInRoom;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LightObjectConfiguration other = (LightObjectConfiguration) obj;
		if (height != other.height)
			return false;
		if (layerNumber != other.layerNumber)
			return false;
		if (lightObjectName == null) {
			if (other.lightObjectName != null)
				return false;
		} else if (!lightObjectName.equals(other.lightObjectName))
			return false;
		if (width != other.width)
			return false;
		if (xOffsetInRoom != other.xOffsetInRoom)
			return false;
		if (yOffsetInRoom != other.yOffsetInRoom)
			return false;
		return true;
	}
	
	
}
