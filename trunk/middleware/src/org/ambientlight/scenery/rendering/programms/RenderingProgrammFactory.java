package org.ambientlight.scenery.rendering.programms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.rendering.Renderer;
import org.ambientlight.scenery.rendering.effects.RenderingEffect;
import org.ambientlight.scenery.rendering.effects.RenderingEffectFactory;
import org.ambientlight.scenery.rendering.effects.transitions.FadeInTransition;
import org.ambientlight.scenery.rendering.programms.configuration.RenderingProgrammConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.rendering.util.ImageUtil;

public class RenderingProgrammFactory implements
		ITransitionEffectFinishedListener {

	RenderingEffectFactory effectFactory;

	public RenderingProgrammFactory(RenderingEffectFactory effectFactory) {
		this.effectFactory = effectFactory;
	}

	List<LightObject> queueDeleteLightObjects = new ArrayList<LightObject>();

	
	public void addLightObjectToRender(Renderer renderer,
			LightObject lightObject, FadeInTransition transition) {

		if (lightObject.getConfiguration().currentRenderingProgrammConfiguration.powerState == false) {
			return;
		}

		// create SimpleColor
		RenderingProgramm renderProgram = null;
		if (lightObject.getConfiguration().currentRenderingProgrammConfiguration instanceof SimpleColorRenderingProgramConfiguration) {
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) lightObject
					.getConfiguration().currentRenderingProgrammConfiguration;
			Color simpleColor = new Color(config.getR(), config.getG(),
					config.getB());
			renderProgram = new SimpleColor(simpleColor);
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

	
	public void updatePowerStateForLightObject(Renderer renderer,
			LightObject lightObject, Boolean powerState) {
		if(lightObject.getConfiguration().currentRenderingProgrammConfiguration.powerState==powerState){
			System.out.println("RenderingProgrammFactory: lightObject"+ lightObject.getConfiguration().lightObjectName+"already set to: "+powerState);
			return;
		}
		
		lightObject.getConfiguration().currentRenderingProgrammConfiguration.powerState = powerState;
		if (powerState == false) {

			// set fadeout effect
			RenderingEffect effect = effectFactory
					.getFadeOutEffect(lightObject);
			RenderingProgramm renderProgram = renderer
					.getProgramForLightObject(lightObject);
			renderProgram.addEffect(effect);
			renderer.setRenderTaskForLightObject(lightObject, renderProgram);

			// and set to deletion queue after effect has finished rendering
			this.queueDeleteLightObjects.add(lightObject);
		} else {
			//maybe the light should be asyncrounously removed. we will keep the light and remove it from the deletion list.
			this.queueDeleteLightObjects.remove(lightObject);
			this.addLightObjectToRender(renderer, lightObject,effectFactory.getFadeInEffect(lightObject));
		}
	}

	
	public void updateRenderingConfigurationForLightObject(Renderer renderer,
			RenderingProgrammConfiguration newConfig, LightObject lightObject) {
		renderer.removeRenderTaskForLightObject(lightObject);
		lightObject.getConfiguration().currentRenderingProgrammConfiguration = newConfig;
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
