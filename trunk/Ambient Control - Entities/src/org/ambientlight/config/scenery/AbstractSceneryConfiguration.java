package org.ambientlight.config.scenery;

import java.io.Serializable;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.codehaus.jackson.annotate.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class AbstractSceneryConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.STRING)
	@Presentation(name = "Szenario Name", position = 0)
	public String id;
}
