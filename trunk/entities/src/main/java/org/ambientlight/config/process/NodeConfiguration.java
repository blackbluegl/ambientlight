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
import org.ambientlight.config.process.handler.actor.RenderingProgrammChangeHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SceneryHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SwitchableHandlerConfiguration;
import org.ambientlight.config.process.handler.event.SensorToTokenConfiguration;
import org.ambientlight.config.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.ExpressionConfiguration;


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
			@Value(displayNewClassInstance = "RGB-LED Konfiguration ändern", newClassInstanceType = RenderingProgrammChangeHandlerConfiguration.class),
			@Value(displayNewClassInstance = "Powerstate ändern (erweitert)", newClassInstanceType = SwitchableHandlerConfiguration.class),
			@Value(displayNewClassInstance = "Powerstate ändern (einfach)", newClassInstanceType = SimplePowerStateHandlerConfiguration.class),
			@Value(displayNewClassInstance = "Scenery Event auslösen", newClassInstanceType = SceneryHandlerConfiguration.class),
			@Value(displayNewClassInstance = "Sensor auslesen", newClassInstanceType = SensorToTokenConfiguration.class),
			@Value(displayNewClassInstance = "Mathematische Funktion", newClassInstanceType = ExpressionConfiguration.class),
			@Value(displayNewClassInstance = "Verzweigung", newClassInstanceType = DecisionHandlerConfiguration.class) })
	public AbstractActionHandlerConfiguration actionHandler;

	public List<Integer> nextNodeIds = new ArrayList<Integer>();
}
