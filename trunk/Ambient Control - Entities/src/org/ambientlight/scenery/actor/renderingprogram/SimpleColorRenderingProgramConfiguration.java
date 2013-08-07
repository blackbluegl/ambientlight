package org.ambientlight.scenery.actor.renderingprogram;

import org.ambientlight.annotations.ClassDescription;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Group;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@ClassDescription(groups = { @Group(description = "Durch setzen der Farbe kann die Lichtstimmung aller farbigen Lichtobjekte verändert werden.", name = "ALLGEMEIN", position = 1) })
@XStreamAlias("simpleColorRenderingProgram")
public class SimpleColorRenderingProgramConfiguration extends RenderingProgramConfiguration implements Cloneable {

	@Presentation(name = "Farbe", groupPosition = 1)
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
