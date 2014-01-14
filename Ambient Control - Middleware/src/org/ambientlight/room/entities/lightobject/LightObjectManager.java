package org.ambientlight.room.entities.lightobject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.room.entities.lightobject.LightObjectConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.device.led.StripePart;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffect;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffectFactory;
import org.ambientlight.room.entities.lightobject.effects.transitions.FadeInTransition;
import org.ambientlight.room.entities.lightobject.programms.RenderingProgramm;
import org.ambientlight.room.entities.lightobject.programms.SimpleColor;
import org.ambientlight.room.entities.lightobject.programms.Sunset;
import org.ambientlight.room.entities.lightobject.programms.Tron;


public class LightObjectManager {

	private LightObjectManagerConfiguration config;

	private RenderingEffectFactory effectFactory;

	private List<LightObject> lightObjects;

	private BufferedImage roomBitMap;



	public LightObjectManager(LightObjectManagerConfiguration config, RenderingEffectFactory effectFactory) {
		this.effectFactory = effectFactory;
		for (LightObjectConfiguration currentItemConfiguration : roomConfig.lightObjectConfigurations.values()) {
			lightObjects.add(this.initializeLightObject(currentItemConfiguration, room.getAllStripePartsInRoom()));
		}

	}


	public BufferedImage getRoomBitMap() {
		return roomBitMap;
	}


	public void setRoomBitMap(BufferedImage roomBitMap) {
		this.roomBitMap = roomBitMap;
	}


	public List<LightObject> getLightObjectsInRoom() {
		return lightObjects;
	}


	public LightObject getLightObjectByName(String name) {
		for (LightObject current : this.lightObjects) {
			if (name.equals(current.configuration.getId()))
				return current;
		}
		return null;
	}


	public void setLightObjectsInRoom(List<LightObject> lightObjectsInRoom) {
		this.lightObjects = lightObjectsInRoom;
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
			System.out.println("RenderingProgrammFactory: lightObject" + lightObject.configuration.getId()
					+ " already set to: " + powerState);
			return;
		}

		Persistence.beginTransaction();
		lightObject.configuration.setPowerState(powerState);
		Persistence.commitTransaction();

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

		Persistence.beginTransaction();

		lightObject.configuration.setRenderProgram(newConfig);

		Persistence.commitTransaction();
		renderer.removeRenderTaskForLightObject(lightObject);

		this.addLightObjectToRender(renderer, lightObject, effectFactory.getFadeInEffect(lightObject));

		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();

	}


	private LightObject initializeLightObject(LightObjectConfiguration lightObjectConfig, List<StripePart> allStripePartsInRoom) {
		List<StripePart> stripePartsInLightObject = this.getStripePartsFromRoomForLightObject(allStripePartsInRoom,
				lightObjectConfig);

		return new LightObject(lightObjectConfig, stripePartsInLightObject);
	}


	private List<StripePart> getStripePartsFromRoomForLightObject(List<StripePart> stripesInRoom,
			LightObjectConfiguration configuration) {
		int minPositionX = configuration.xOffsetInRoom;
		int minPositionY = configuration.yOffsetInRoom;
		int maxPoistionX = configuration.xOffsetInRoom + configuration.width;
		int maxPositionY = configuration.yOffsetInRoom + configuration.height;

		List<StripePart> result = new ArrayList<StripePart>();

		for (StripePart currentSubStripe : stripesInRoom) {
			if (minPositionX > currentSubStripe.configuration.startXPositionInRoom) {
				continue;
			}
			if (minPositionY > currentSubStripe.configuration.startYPositionInRoom) {
				continue;
			}
			if (maxPoistionX < currentSubStripe.configuration.endXPositionInRoom) {
				continue;
			}
			if (maxPositionY < currentSubStripe.configuration.endYPositionInRoom) {
				continue;
			}
			result.add(currentSubStripe);
		}

		return result;
	}
}
