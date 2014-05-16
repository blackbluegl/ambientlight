package org.ambientlight.config.process.handler.expression;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;


@HandlerDataTypeValidation(consumes = { DataTypeValidation.NUMERIC }, generates = DataTypeValidation.NUMERIC)
public class ExpressionHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.BEAN)
	@Presentation(name = "Mathematische Funktion", position = 0, description = "Ist der Ausdruck wahr wird der Knoten über die erste Verbindung verlassen. Ist der Ausdruck falsch wird die alternative Verbindung verwendet.")
	public ExpressionConfiguration expressionConfiguration;
}
