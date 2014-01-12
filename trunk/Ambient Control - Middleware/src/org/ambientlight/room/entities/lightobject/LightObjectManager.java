package org.ambientlight.room.entities.lightobject;

import java.awt.Color;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffect;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffectFactory;
import org.ambientlight.room.entities.lightobject.effects.transitions.FadeInTransition;
import org.ambientlight.room.entities.lightobject.programms.RenderingProgramm;
import org.ambientlight.room.entities.lightobject.programms.SimpleColor;
import org.ambientlight.room.entities.lightobject.programms.Sunset;
import org.ambientlight.room.entities.lightobject.programms.Tron;


public class LightObjectManager {

	RenderingEffectFactory effectFactory;


	public LightObjectManager(RenderingEffectFactory effectFactory) {
		this.effectFactory = effectFactory;
	}


	private void addLightObjectToRender(Renderer renderer, LightObject lightObject, FadeInTransition transition) {

		if (lightObject.configuration.getPowerState() == false)
			return;


		RenderingProgramm renderProgram = null;

		// create SimpleColor
		if (lightObject.configuration.getRenderingProgramConfiguration() instanceof SimpleColorRenderingProgramConfiguration) {
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) lightObject.configuration
					.getRenderingProgramConfiguration();
			Color simpleColor = new Color(config.rgb);
			renderProgram = new SimpleColor(simpleColor);
		}

		// create Tron
		if (lightObject.configuration.getRenderingProgramConfiguration() instanceof TronRenderingProgrammConfiguration) {
			TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) lightObject.configuration
					.getRenderingProgramConfiguration();
			Color color = new Color(config.rgb);
			renderProgram = new Tron(lightObject, color, config.lightImpact, config.tailLength, config.sparkleStrength,
					config.sparkleSize, config.speed, config.lightPointAmount);
		}

		// create Sunset
		if (lightObject.configuration.getRenderingProgramConfiguration() instanceof SunSetRenderingProgrammConfiguration) {
			SunSetRenderingProgrammConfiguration config = (SunSetRenderingProgrammConfiguration) lightObject.configuration
					.getRenderingProgramConfiguration();
			renderProgram = new Sunset(config.duration, config.position, config.sunStartX, config.sunStartY, config.sunSetX,
					config.sizeOfSun, config.gamma);
		}

		renderProgram.addEffect(transition);

		renderer.addRenderTaskForLightObject(lightObject, renderProgram);

	}


	public void setPowerStateForLightObject(Renderer renderer, LightObject lightObject, Boolean powerState) {

		if (lightObject.configuration.getPowerState() == powerState) {
			System.out.println("RenderingProgrammFactory: lightObject" + lightObject.configuration.getName()
					+ " already set to: " + powerState);
			return;
		}

		RoomConfigurationFactory.beginTransaction();
		lightObject.configuration.setPowerState(powerState);
		RoomConfigurationFactory.commitTransaction();

		if (powerState == false) {

			// set fadeout effect
			RenderingEffect effect = effectFactory.getFadeOutEffect(lightObject);
			RenderingProgramm renderProgram = renderer.getProgramForLightObject(lightObject);
			renderProgram.addEffect(effect);
			renderer.addRenderTaskForLightObject(lightObject, renderProgram);

		} else {
			this.addLightObjectToRender(renderer, lightObject, effectFactory.getFadeInEffect(lightObject));
		}
	}


	public void setRenderingConfigurationForLightObject(Renderer renderer, RenderingProgramConfiguration newConfig,
			LightObject lightObject) {

		RoomConfigurationFactory.beginTransaction();

		lightObject.configuration.setRenderProgram(newConfig);

		RoomConfigurationFactory.commitTransaction();
		renderer.removeRenderTaskForLightObject(lightObject);

		this.addLightObjectToRender(renderer, lightObject, effectFactory.getFadeInEffect(lightObject));

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

	}
}
