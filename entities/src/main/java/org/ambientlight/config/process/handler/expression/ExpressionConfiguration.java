package org.ambientlight.config.process.handler.expression;

import java.io.Serializable;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;

public class ExpressionConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.EXPRESSION)
	@Presentation(name = "Mathematischer Ausdruck", position = 0, description = "Durch Variablen ist der Zugriff auf Werte von "
			+ "Sensoren möglich. Variablen beginnen mit \'#{\'. Um auf Prozessdaten des Vorgängerkontens zuzugreifen kann"
			+ " #{tokenValue} verwendet werden.\n\nBolsche Ausdrücke werden in JEVAL mit "
			+ "1.0 als wahr und mit 0.0 als falsch bezeichnet.")
	@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA, DataTypeValidation.SENSOR }, generates = DataTypeValidation.NUMERIC)
	public String expression = "";
}
