package org.ambientlight.process.handler.expression;

import org.ambientlight.process.handler.DataTypeValidation;
import org.ambientlight.process.handler.HandlerDataTypeValidation;


@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA, DataTypeValidation.BOOLEAN, DataTypeValidation.NUMERIC }, generates = DataTypeValidation.BOOLEAN)

public class DecisionHandlerConfiguration extends ExpressionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

}
