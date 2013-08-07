package org.ambientlight.process.handler.expression;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.annotations.Value;
import org.ambientlight.process.handler.AbstractActionHandlerConfiguration;


public class ExpressionHandlerConfiguration extends AbstractActionHandlerConfiguration {

	@TypeDef(fieldType = FieldType.BEAN)
	@Presentation(name = "Boolscher Ausdruck", description = "Ist der Ausdruck wahr wird der Knoten über die erste Verbindung verlassen. Ist der Ausdruck falsch wird die alternative Verbindung verwendet.")
	@AlternativeValues(values = { @Value(displayName = "Neuer Ausdruck", value = "org.ambientlight.process.handler.expression.ExpressionConfiguration") })
	public ExpressionConfiguration expressionConfiguration;
}
