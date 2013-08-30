package org.ambientlight.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.process.events.EventConfiguration;


public class EventProcessConfiguration extends ProcessConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.SIMPLE_LIST)
	@Presentation(name = "EventTrigger", position = 1)
	@AlternativeValues(values = {
			@Value(displayName = "Schalter Ereignis", value = "org.ambientlight.process.events.SwitchEventConfiguration"),
			@Value(displayName = "Szenerie Ereignis", value = "org.ambientlight.process.events.SceneryEntryEventConfiguration") })
	public List<EventConfiguration> eventTriggerConfigurations = new ArrayList<EventConfiguration>();

};
