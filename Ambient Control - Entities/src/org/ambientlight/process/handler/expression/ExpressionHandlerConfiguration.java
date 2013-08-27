package org.ambientlight.process.handler.expression;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.process.validation.HandlerDataTypeValidation;


@HandlerDataTypeValidation(consumes = { DataTypeValidation.NUMERIC }, generates = DataTypeValidation.NUMERIC)
public class ExpressionHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.BEAN)
	@Presentation(name = "Mathematische Funktion", description = "Ist der Ausdruck wahr wird der Knoten über die erste Verbindung verlassen. Ist der Ausdruck falsch wird die alternative Verbindung verwendet.")
	@AlternativeValues(values = { @Value(displayName = "Neuer Ausdruck", value = "org.ambientlight.process.handler.expression.ExpressionConfiguration") })
	public ExpressionConfiguration expressionConfiguration;
}
