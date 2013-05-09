package org.ambientlight.scenery.actor.rendering.programms.configuration;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.scenery.EntitiyConfiguration;
import org.ambientlight.scenery.actor.ActorConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("simpleColorRenderingProgram")
public class SimpleColorRenderingProgramConfiguration extends ActorConfiguration implements Cloneable {

	@Presentation(name="Farbe")
	@TypeDef(fieldType=FieldType.COLOR,min="0",max="255")
	public int rgb;

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
