package org.ambientlight.config.process.handler.expression;

import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.config.process.validation.HandlerDataTypeValidation;


@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA, DataTypeValidation.BOOLEAN, DataTypeValidation.NUMERIC }, generates = DataTypeValidation.BOOLEAN)
public class DecisionHandlerConfiguration extends ExpressionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

}
