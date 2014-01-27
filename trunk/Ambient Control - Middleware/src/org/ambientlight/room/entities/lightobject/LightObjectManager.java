package org.ambientlight.room.entities.lightobject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ambientlight.callback.CallBackManager;
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

	private Map<String, RenderObject> lightObjectRenderObjects;

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
		for (LightObject currentItemConfiguration : config.lightObjectConfigurations.values()) {

			List<StripePart> stripePartsInLightObject = this.getStripePartsFromRoomForLightObject(allStripePartsInRoom,
					currentItemConfiguration);
			lightObjectRenderObjects.put(currentItemConfiguration.getId(), new RenderObject(currentItemConfiguration,
					stripePartsInLightObject));
		}
	}


	public BufferedImage getRoomBitMap() {
		return pixelMap;
	}


	public void setRoomBitMap(BufferedImage roomBitMap) {
		this.pixelMap = roomBitMap;
	}


	public List<RenderObject> getLightObjectsInRoom() {
		return lightObjectRenderObjects;
	}


	public RenderObject getLightObjectByName(String name) {
		for (RenderObject current : this.lightObjectRenderObjects) {
			if (name.equals(current.lightObject.getId()))
				return current;
		}
		return null;
	}


	public void setLightObjectsInRoom(List<RenderObject> lightObjectsInRoom) {
		this.lightObjectRenderObjects = lightObjectsInRoom;
	}


	private void addLightObjectToRender(Renderer renderer, RenderObject lightObject, FadeInTransition transition) {

		if (lightObject.lightObject.getPowerState() == false)
			return;

		RenderingProgramm renderProgram = null;

		// create SimpleColor
		if (lightObject.lightObject.getRenderingProgramConfiguration() instanceof SimpleColorRenderingProgramConfiguration) {
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) lightObject.lightObject
					.getRenderingProgramConfiguration();
			Color simpleColor = new Color(config.rgb);
			renderProgram = new SimpleColor(simpleColor);
		}

		// create Tron
		if (lightObject.lightObject.getRenderingProgramConfiguration() instanceof TronRenderingProgrammConfiguration) {
			TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) lightObject.lightObject
					.getRenderingProgramConfiguration();
			Color color = new Color(config.rgb);
			renderProgram = new Tron(lightObject, color, config.lightImpact, config.tailLength, config.sparkleStrength,
					config.sparkleSize, config.speed, config.lightPointAmount);
		}

		// create Sunset
		if (lightObject.lightObject.getRenderingProgramConfiguration() instanceof SunSetRenderingProgrammConfiguration) {
			SunSetRenderingProgrammConfiguration config = (SunSetRenderingProgrammConfiguration) lightObject.lightObject
					.getRenderingProgramConfiguration();
			renderProgram = new Sunset(config.duration, config.position, config.sunStartX, config.sunStartY, config.sunSetX,
					config.sizeOfSun, config.gamma);
		}

		renderProgram.addEffect(transition);

		renderer.addRenderTaskForLightObject(lightObject, renderProgram);

	}


	public void setPowerStateForLightObject(RenderObject lightObject, Boolean powerState) {

		if (lightObject.lightObject.getPowerState() == powerState) {
			System.out.println("RenderingProgrammFactory: lightObject" + lightObject.lightObject.getId() + " already set to: "
					+ powerState);
			return;
		}

		Persistence.beginTransaction();
		lightObject.lightObject.setPowerState(powerState);
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
	2 fragen:
		doch zurück zu persistenz und oder arbeitsobjekte?
				webservice facade benutzt persistenzobjekte?

						public void setRenderingConfiguration(RenderingProgramConfiguration newConfig, String id) {

		RenderObject renderObject = lightObjectRenderObjects.get(id);

		if (renderObject == null) {
			System.out.println("LightObjectManager setRenderingConfiguration: unknown renderable Id: " + id);
			return;
		}

		Persistence.beginTransaction();

		renderObject.lightObject.setRenderProgram(newConfig);

		Persistence.commitTransaction();
		renderer.removeRenderTaskForLightObject(renderObject);

		this.addLightObjectToRender(renderer, renderObject, effectFactory.getFadeInEffect(renderObject));

		callBackMananger.roomConfigurationChanged();

	}


	private List<StripePart> getStripePartsFromRoomForLightObject(List<StripePart> stripesInRoom, LightObject configuration) {
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
