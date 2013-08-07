package org.ambientlight.process.handler.expression;

import java.io.Serializable;

import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;


public class ExpressionConfiguration implements Serializable {

	@TypeDef(fieldType = FieldType.EXPRESSION)
	@Presentation(name = "Mathematischer Ausdruck", description = "Durch Variablen ist der Zugriff auf Werte von "
			+ "Sensoren möglich. Variablen beginnen mit \'#{\'. Um auf Prozessdaten des Vorgängerkontens zuzugreifen kann"
			+ " #{tokenValue} verwendet werden.\n\nBolsche Ausdrücke werden in JEVAL mit "
			+ "1.0 als wahr und mit 0.0 als falsch bezeichnet.")
	@AlternativeValues(valueBinding = "eventGeneratorConfigurations.name")
	public String expression = "";
}
