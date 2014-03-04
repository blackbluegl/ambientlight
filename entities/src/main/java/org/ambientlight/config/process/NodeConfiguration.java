package org.ambientlight.config.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;


public class NodeConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public int id;

	/*
	 * Actionhandler that processes the node. If nextNodeId within is set to
	 * null the process will stop at this node
	 */
	@Presentation(description = "Aktion auswählen", position = 1)
	@TypeDef(fieldType = FieldType.BEAN)
	@AlternativeValues(values = {
			@Value(displayName = "RGB-LED Konfiguration ändern", value = "org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration"),
			@Value(displayName = "Powerstate ändern (erweitert)", value = "org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration"),
			@Value(displayName = "Powerstate ändern (einfach)", value = "org.ambientlight.process.handler.actor.SimplePowerStateHandlerConfiguration"),
			@Value(displayName = "Event auslesen", value = "org.ambientlight.process.handler.event.EventGeneratorSensorAdapterConfiguration"),
			@Value(displayName = "Event in Boolschen Ausdruck wandeln", value = "org.ambientlight.process.handler.event.EventToBooleanHandlerConfiguration"),
			@Value(displayName = "Event auslösen", value = "org.ambientlight.process.handler.event.FireEventHandlerConfiguration"),
			@Value(displayName = "Mathematische Funktion", value = "org.ambientlight.process.handler.expression.ExpressionConfiguration"),
			@Value(displayName = "Verzweigung", value = "org.ambientlight.process.handler.expression.DecisionHandlerConfiguration") })
	public AbstractActionHandlerConfiguration actionHandler;

	public List<Integer> nextNodeIds = new ArrayList<Integer>();
}
