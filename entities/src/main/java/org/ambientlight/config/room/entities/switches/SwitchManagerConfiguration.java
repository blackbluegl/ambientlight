package org.ambientlight.config.room.entities.switches;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.switches.Switch;
import org.ambientlight.ws.EntityIdDeserializer;
import org.ambientlight.ws.EntityIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


public class SwitchManagerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSerialize(keyUsing = EntityIdSerializer.class)
	@JsonDeserialize(keyUsing = EntityIdDeserializer.class)
	public Map<EntityId, Switch> switches = new HashMap<EntityId, Switch>();
}
