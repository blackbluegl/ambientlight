package org.ambientlight.config.room.entities.lightobject.renderingprogram;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;

import com.thoughtworks.xstream.annotations.XStreamAlias;



@XStreamAlias("simpleColorRenderingProgram")
public class SimpleColorRenderingProgramConfiguration extends RenderingProgramConfiguration implements Cloneable {

	private static final long serialVersionUID = 1L;

	@Presentation(name = "Farbe", position = 0)
	@TypeDef(fieldType = FieldType.COLOR, min = "0", max = "255")
	public int rgb;


	@Override
	public SimpleColorRenderingProgramConfiguration clone() {
		try {
			return (SimpleColorRenderingProgramConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			// this should not happen
			e.printStackTrace();
		}
		return null;
	}

}
