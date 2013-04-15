package org.ambientlight.scenery.rendering.programms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.scenery.SceneryConfiguration;
import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.rendering.Renderer;
import org.ambientlight.scenery.rendering.effects.RenderingEffect;
import org.ambientlight.scenery.rendering.effects.RenderingEffectFactory;
import org.ambientlight.scenery.rendering.effects.transitions.FadeInTransition;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.TronRenderingProgrammConfiguration;
import org.ambientlight.scenery.rendering.util.ImageUtil;

public class RenderingProgrammFactory implements
		ITransitionEffectFinishedListener {

	RenderingEffectFactory effectFactory;

	public RenderingProgrammFactory(RenderingEffectFactory effectFactory) {
		this.effectFactory = effectFactory;
	}

	List<LightObject> queueDeleteLightObjects = new ArrayList<LightObject>();

	
	public void addLightObjectToRender(Renderer renderer,LightObject lightObject, FadeInTransition transition) {
		
		String currentScenery = AmbientControlMW.getRoomConfig().currentScenery;
		
		if (lightObject.getConfiguration().getSceneryConfigurationBySceneryName(currentScenery).powerState == false) {
			return;
		}

		RenderingProgramm renderProgram = null;
		
		// create SimpleColor
		if (lightObject.getConfiguration().getSceneryConfigurationBySceneryName(currentScenery) instanceof SimpleColorRenderingProgramConfiguration) {
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) lightObject
					.getConfiguration().getSceneryConfigurationBySceneryName(currentScenery);
			Color simpleColor = new Color(config.getR(), config.getG(),
					config.getB());
			renderProgram = new SimpleColor(simpleColor);
		}
		
		// create Tron
		if (lightObject.getConfiguration().getSceneryConfigurationBySceneryName(currentScenery) instanceof TronRenderingProgrammConfiguration) {
			TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) lightObject
					.getConfiguration().getSceneryConfigurationBySceneryName(currentScenery);
			renderProgram = new Tron(lightObject,config.getSpeed(),config.getLightPointAmount());
		}
		
		
		renderProgram.addEffect(transition);

		renderer.setRenderTaskForLightObject(lightObject, renderProgram);

	}

	
	public void addAllLightObjectsInRoomToRenderer(Renderer renderer,
			List<LightObject> lightObjects) {
		for (LightObject current : lightObjects) {
			this.addLightObjectToRender(renderer, current,effectFactory.getFadeInEffect(current));
		}
	}

	
	public void updatePowerStateForLightObject(Renderer renderer, LightObject lightObject, Boolean powerState) {
		String currentScenery = AmbientControlMW.getRoomConfig().currentScenery;

		if (lightObject.getConfiguration().getSceneryConfigurationBySceneryName(currentScenery).powerState == powerState) {
			System.out.println("RenderingProgrammFactory: lightObject" + lightObject.getConfiguration().name + "already set to: "
					+ powerState);
			return;
		}

		lightObject.getConfiguration().getSceneryConfigurationBySceneryName(currentScenery).powerState = powerState;
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

	
	public void updateRenderingConfigurationForLightObject(Renderer renderer,
			SceneryConfiguration newConfig, LightObject lightObject) {
		String currentScenery = AmbientControlMW.getRoomConfig().currentScenery;
		renderer.removeRenderTaskForLightObject(lightObject);
		lightObject.getConfiguration().sceneryConfigurationBySzeneryName.remove(currentScenery);
		lightObject.getConfiguration().sceneryConfigurationBySzeneryName.put(currentScenery, newConfig);
		this.addLightObjectToRender(renderer, lightObject,effectFactory.getFadeInEffect(lightObject));
	}

	
	@Override
	public  void lightObjectTransitionEffectFinished(Renderer renderer,
			LightObject lightObject) {
		if (this.queueDeleteLightObjects.contains(lightObject)) {
			renderer.removeRenderTaskForLightObject(lightObject);
			lightObject.setPixelMap(ImageUtil.getBlackImage(lightObject
					.getPixelMap()));
			this.queueDeleteLightObjects.remove(lightObject);
		}
	}

}
