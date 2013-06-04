package org.ambientlight.scenery.actor.renderingprogram;

import org.ambientlight.annotations.FieldType;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;
import org.ambientlight.scenery.actor.ActorConductConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("TronRenderingProgram")
public class TronRenderingProgrammConfiguration extends RenderingProgramConfiguration implements Cloneable{
	@Presentation(position="0",name="Anzahl der Lichtpunkte")
	@TypeDef(min="1",max="10")
	public int lightPointAmount;
	
	@Presentation(position="1",name="Helligkeit der Lichtpunkte")
	@TypeDef(min="0.0",max="1.0")
	public double lightImpact;
	
	@Presentation(position="2",name="Länge der Lichtpunkte")
	@TypeDef(min="0.0",max="1.0")
	public double tailLength;
	
	@Presentation(position="3",name="Helligkeit des Funkeln")
	@TypeDef(min="0.0",max="1.0")
	public double sparkleStrength;
	
	@Presentation(position="4",name="Länge des Funkeln")
	@TypeDef(min="0.0",max="1.0")
	public double sparkleSize;
	
	@Presentation(position="5",name="Geschwindigkeit")
	@TypeDef(min="0.0",max="20.0")
	public double speed;
	
	@Presentation(position="6",name="Hintergrundfarbe")
	@TypeDef(fieldType=FieldType.COLOR)
	public int rgb;
	
	public TronRenderingProgrammConfiguration clone(){
		try {
			return (TronRenderingProgrammConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			//this should not happen
			e.printStackTrace();
		}
		return null;
	}
}
