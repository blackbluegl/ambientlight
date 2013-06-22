package org.ambientlight.scenery.rendering;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.room.entities.LightObject;
import org.ambientlight.scenery.actor.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.scenery.rendering.effects.RenderingEffect;
import org.ambientlight.scenery.rendering.effects.RenderingEffectFactory;
import org.ambientlight.scenery.rendering.effects.transitions.FadeInTransition;
import org.ambientlight.scenery.rendering.programms.ITransitionEffectFinishedListener;
import org.ambientlight.scenery.rendering.programms.RenderingProgramm;
import org.ambientlight.scenery.rendering.programms.SimpleColor;
import org.ambientlight.scenery.rendering.programms.Tron;
import org.ambientlight.scenery.rendering.util.ImageUtil;


public class RenderControl implements ITransitionEffectFinishedListener {

	RenderingEffectFactory effectFactory;


	public RenderControl(RenderingEffectFactory effectFactory) {
		this.effectFactory = effectFactory;
	}

	List<LightObject> queueDeleteLightObjects = new ArrayList<LightObject>();


	public void addLightObjectToRender(Renderer renderer, LightObject lightObject, FadeInTransition transition) {

		if (lightObject.configuration.getPowerState() == false)
			return;

		RenderingProgramm renderProgram = null;

		// create SimpleColor
		if (lightObject.configuration.renderingProgramConfiguration instanceof SimpleColorRenderingProgramConfiguration) {
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) lightObject.configuration.renderingProgramConfiguration;
			Color simpleColor = new Color(config.rgb);
			renderProgram = new SimpleColor(simpleColor);
		}

		// create Tron
		if (lightObject.configuration.renderingProgramConfiguration instanceof TronRenderingProgrammConfiguration) {
			TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) lightObject.configuration.renderingProgramConfiguration;
			Color color = new Color(config.rgb);
			renderProgram = new Tron(lightObject, color, config.lightImpact, config.tailLength, config.sparkleStrength,
					config.sparkleSize, config.speed, config.lightPointAmount);
		}

		renderProgram.addEffect(transition);

		renderer.setRenderTaskForLightObject(lightObject, renderProgram);

	}


	public void addAllLightObjectsInRoomToRenderer(Renderer renderer, List<LightObject> lightObjects) {
		for (LightObject current : lightObjects) {
			this.addLightObjectToRender(renderer, current, effectFactory.getFadeInEffect(current));
		}
	}


	public void updatePowerStateForLightObject(Renderer renderer, LightObject lightObject, Boolean powerState) {

		if (lightObject.configuration.getPowerState() == powerState) {
			System.out.println("RenderingProgrammFactory: lightObject" + lightObject.configuration.getName() + "already set to: "
					+ powerState);
			return;
		}

		lightObject.configuration.setPowerState(powerState);
		if (powerState == false) {

			// set fadeout effect
			RenderingEffect effect = effectFactory.getFadeOutEffect(lightObject);
			RenderingProgramm renderProgram = renderer.getProgramForLightObject(lightObject);
			renderProgram.addEffect(effect);
			renderer.setRenderTaskForLightObject(lightObject, renderProgram);

			// and set to deletion queue after effect has finished rendering
			this.queueDeleteLightObjects.add(lightObject);
		} else {
			// maybe the light should be asyncrounously removed. we will keep
			// the light and remove it from the deletion list.
			this.queueDeleteLightObjects.remove(lightObject);
			this.addLightObjectToRender(renderer, lightObject, effectFactory.getFadeInEffect(lightObject));
		}
	}


	public void updateRenderingConfigurationForLightObject(Renderer renderer, RenderingProgramConfiguration newConfig,
			LightObject lightObject) {
		renderer.removeRenderTaskForLightObject(lightObject);
		lightObject.configuration.renderingProgramConfiguration = newConfig;
		this.addLightObjectToRender(renderer, lightObject, effectFactory.getFadeInEffect(lightObject));
	}


	@Override
	public void lightObjectTransitionEffectFinished(Renderer renderer, LightObject lightObject) {
		if (this.queueDeleteLightObjects.contains(lightObject)) {
			renderer.removeRenderTaskForLightObject(lightObject);
			lightObject.setPixelMap(ImageUtil.getPaintedImage(lightObject.getPixelMap(), Color.black));
			this.queueDeleteLightObjects.remove(lightObject);
		}
	}

}
