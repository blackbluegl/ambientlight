package org.ambientlight.config.room.entities.led.renderingprogram;

import org.ambientlight.annotations.ClassDescription;
import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Group;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("TronRenderingProgram")
@ClassDescription(groups = { @Group(name = "ANIMATION", position = 1), @Group(name = "DARSTELLUNG", position = 2) })
public class TronRenderingProgrammConfiguration extends RenderingProgramConfiguration implements Cloneable {

	@Presentation(position = 0, name = "Anzahl der Lichtpunkte", groupPosition = 1)
	@TypeDef(min = "1", max = "10")
	public int lightPointAmount;

	@Presentation(position = 1, name = "Helligkeit der Lichtpunkte", groupPosition = 2)
	@TypeDef(min = "0.0", max = "1.0")
	public double lightImpact;

	@Presentation(position = 2, name = "Länge der Lichtpunkte", groupPosition = 1)
	@TypeDef(min = "0.0", max = "1.0")
	public double tailLength;

	@Presentation(position = 3, name = "Stärke des Funkeln", groupPosition = 1)
	@TypeDef(min = "0.0", max = "1.0")
	public double sparkleStrength;

	@Presentation(position = 4, name = "Länge des Funkeln", groupPosition = 1)
	@TypeDef(min = "0.0", max = "1.0")
	public double sparkleSize;

	@Presentation(position = 5, name = "Geschwindigkeit", groupPosition = 1)
	@TypeDef(min = "0.0", max = "20.0")
	public double speed;

	@Presentation(position = 6, name = "Hintergrundfarbe", groupPosition = 2)
	@TypeDef(fieldType = FieldType.COLOR)
	public int rgb;


	@Override
	public TronRenderingProgrammConfiguration clone() {
		try {
			return (TronRenderingProgrammConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			// this should not happen
			e.printStackTrace();
		}
		return null;
	}
}
