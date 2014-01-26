package org.ambientlight.room.entities.lightobject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.entities.lightobject.LightObjectConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.device.drivers.AnimateableLedDevice;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.led.LedPoint;
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

	private CallBackManager callBackMananger;

	List<StripePart> allStripePartsInRoom;

	List<LedPoint> ledPointsInRoom;

	private RenderingEffectFactory effectFactory;

	private List<LightObject> lightObjects;

	private BufferedImage pixelMap;

	private Renderer renderer;

	public List<DeviceDriver> devices;


	public LightObjectManager(BufferedImage pixelMap, LightObjectManagerConfiguration config,
			RenderingEffectFactory effectFactory, List<DeviceDriver> devices, Renderer renderer, CallBackManager callBackMananger) {

		this.callBackMananger = callBackMananger;

		this.renderer = renderer;

		this.pixelMap = pixelMap;

		this.effectFactory = effectFactory;

		this.devices = devices;

		this.config = config;
		for (LightObjectConfiguration currentItemConfiguration : config.lightObjectConfigurations.values()) {

			List<StripePart> stripePartsInLightObject = this.getStripePartsFromRoomForLightObject(allStripePartsInRoom,
					currentItemConfiguration);

			lightObjects.add(new LightObject(currentItemConfiguration, stripePartsInLightObject));

		}
	}


	public BufferedImage getRoomBitMap() {
		return pixelMap;
	}


	public void setRoomBitMap(BufferedImage roomBitMap) {
		this.pixelMap = roomBitMap;
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
			System.out.println("RenderingProgrammFactory: lightObject" + lightObject.configuration.getId() + " already set to: "
					+ powerState);
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

		callBackMananger.roomConfigurationChanged();

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


	public List<AnimateableLedDevice> getLedAnimateableDevices() {
		List<AnimateableLedDevice> result = new ArrayList<AnimateableLedDevice>();
		for (DeviceDriver currentDevice : this.devices) {
			if (currentDevice instanceof AnimateableLedDevice) {
				result.add((AnimateableLedDevice) currentDevice);
			}
		}

		return result;
	}
}
