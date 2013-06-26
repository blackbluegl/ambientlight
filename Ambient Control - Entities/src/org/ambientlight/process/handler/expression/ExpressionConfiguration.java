package org.ambientlight.process.handler.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ExpressionConfiguration implements Serializable {

	public String expression = "";
	public List<String> sensorNames = new ArrayList<String>();
}
