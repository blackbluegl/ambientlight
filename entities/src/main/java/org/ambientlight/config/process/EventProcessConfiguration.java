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


public class EventProcessConfiguration extends ProcessConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.SIMPLE_LIST)
	@Presentation(name = "EventTrigger", position = 1)
	@AlternativeValues(values = {
			@Value(displayValue = "Schalter Ereignis", newClassInstanceType = "org.ambientlight.events.SwitchEvent"),
			@Value(displayValue = "Szenerie Ereignis", newClassInstanceType = "org.ambientlight.events.SceneryEntryEvent") })
	public List<BroadcastEvent> eventTriggerConfigurations = new ArrayList<BroadcastEvent>();

};
