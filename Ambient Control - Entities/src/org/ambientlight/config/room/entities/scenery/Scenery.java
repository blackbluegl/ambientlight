package org.ambientlight.config.room.entities.scenery;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;


public class Scenery {

	private static final long serialVersionUID = 1L;

	@TypeDef(fieldType = FieldType.STRING)
	@Presentation(name = "Szenario Name", position = 0)
	public String id;


	@Override
	public String toString() {
		return "Anwenderszenario mit Name: " + id;
	}
}
