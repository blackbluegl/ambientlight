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
			@Value(displayNewClassInstance = "RGB-LED Konfiguration ändern", newClassInstanceType = "org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration"),
			@Value(displayNewClassInstance = "Powerstate ändern (erweitert)", newClassInstanceType = "org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration"),
			@Value(displayNewClassInstance = "Powerstate ändern (einfach)", newClassInstanceType = "org.ambientlight.process.handler.actor.SimplePowerStateHandlerConfiguration"),
			@Value(displayNewClassInstance = "Event auslesen", newClassInstanceType = "org.ambientlight.process.handler.event.EventGeneratorSensorAdapterConfiguration"),
			@Value(displayNewClassInstance = "Event in Boolschen Ausdruck wandeln", newClassInstanceType = "org.ambientlight.process.handler.event.EventToBooleanHandlerConfiguration"),
			@Value(displayNewClassInstance = "Event auslösen", newClassInstanceType = "org.ambientlight.process.handler.event.FireEventHandlerConfiguration"),
			@Value(displayNewClassInstance = "Mathematische Funktion", newClassInstanceType = "org.ambientlight.process.handler.expression.ExpressionConfiguration"),
			@Value(displayNewClassInstance = "Verzweigung", newClassInstanceType = "org.ambientlight.process.handler.expression.DecisionHandlerConfiguration") })
	public AbstractActionHandlerConfiguration actionHandler;

	public List<Integer> nextNodeIds = new ArrayList<Integer>();
}
