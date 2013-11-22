package org.ambientlight.config.scenery.actor.renderingprogram;

import org.ambientlight.annotations.ClassDescription;
import org.ambientlight.annotations.Group;
import org.ambientlight.annotations.Presentation;
import org.ambientlight.annotations.TypeDef;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("sunsetRenderingProgram")
@ClassDescription(groups = { @Group(name = "ALLGEMEIN", position = 1), @Group(name = "POSITION", position = 2),
		@Group(name = "DARSTELLUNG", position = 3) })
public class SunSetRenderingProgrammConfiguration extends RenderingProgramConfiguration {

	public static final long serialVersionUID = 1L;

	@Presentation(position = 0, name = "Dauer (min)", groupPosition = 1)
	@TypeDef(min = "1", max = "90")
	public double duration = 30;

	@Presentation(position = 1, name = "Starten bei", groupPosition = 1)
	@TypeDef(min = "0", max = "1")
	public double position = 0.0;

	@Presentation(position = 1, name = "Größe der Sonne", groupPosition = 3)
	@TypeDef(min = "0.1", max = "3")
	public double sizeOfSun = 1.0;

	@Presentation(position = 0, name = "Start auf X-Achse", groupPosition = 2)
	@TypeDef(min = "0", max = "1")
	public double sunStartX = 0.5;

	@Presentation(position = 1, name = "Start auf Y-Achse", groupPosition = 2)
	@TypeDef(min = "0", max = "1")
	public double sunStartY = 0.5;

	@Presentation(position = 2, name = "Untergang auf X-Achse", groupPosition = 2)
	@TypeDef(min = "0", max = "1")
	public double sunSetX = 0.5;

	@Presentation(position = 0, name = "Gammakorrektur", groupPosition = 3)
	@TypeDef(min = "0", max = "6")
	public double gamma = 2.2;

}
