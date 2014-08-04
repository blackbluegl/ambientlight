package org.ambientlight.config.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.events.BroadcastEvent;
import org.ambientlight.events.SceneryEntryEvent;
import org.ambientlight.events.SwitchEvent;


public class EventProcessConfiguration extends ProcessConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.SIMPLE_LIST)
	@Presentation(name = "EventTrigger", position = 1)
	@AlternativeValues(values = {
			@Value(displayNewClassInstance = "Schalter Ereignis", newClassInstanceType = SwitchEvent.class),
			@Value(displayNewClassInstance = "Szenerie Ereignis", newClassInstanceType = SceneryEntryEvent.class) })
	public List<BroadcastEvent> eventTriggerConfigurations = new ArrayList<BroadcastEvent>();

};
