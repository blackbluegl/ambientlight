package org.ambientlight.process.handler;

import java.util.Arrays;
import java.util.List;


public enum DataTypeValidation {
	CONSUMES_NO_DATA("Konsumiert keine Eingangsdaten"), CREATES_NO_DATA("Erzeugt keine Daten"), EVENT("Events"), BOOLEAN(
			"Boolscher Ausdruck"), NUMERIC("Numerischer Wert");

	public String description;


	DataTypeValidation(String description) {
		this.description = description;
	}


	public static DataTypeValidation[] getCompatibleInputTypes(DataTypeValidation outputPreviousNode) {
		switch (outputPreviousNode) {

		case BOOLEAN:
			return new DataTypeValidation[] { DataTypeValidation.BOOLEAN, DataTypeValidation.CONSUMES_NO_DATA };

		case EVENT:
			return new DataTypeValidation[] { DataTypeValidation.EVENT, DataTypeValidation.CONSUMES_NO_DATA };

		case CREATES_NO_DATA:
			return new DataTypeValidation[] { DataTypeValidation.CONSUMES_NO_DATA };

		case NUMERIC:
			return new DataTypeValidation[] { DataTypeValidation.NUMERIC, DataTypeValidation.CONSUMES_NO_DATA };

		default:
			throw new IllegalArgumentException(outputPreviousNode.toString() + "is no OutputFormat");
		}
	}


	public static boolean validate(DataTypeValidation[] actualNodeInputValues, DataTypeValidation previousNodeOutputValues) {
		List<DataTypeValidation> validateInputTypes = Arrays.asList(getCompatibleInputTypes(previousNodeOutputValues));
		for (DataTypeValidation currentInputType : actualNodeInputValues) {
			if (validateInputTypes.contains(currentInputType))
				return true;
		}
		return false;
	}
}