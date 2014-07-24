package org.ambientlight.room.entities.lightobject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import org.ambientlight.Manager;
import org.ambientlight.Persistence;
import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.entities.lightobject.LightObjectManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.device.drivers.AnimateableLedDevice;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.LedPointDeviceDriver;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.device.led.StripePart;
import org.ambientlight.room.entities.FeatureFacade;
import org.ambientlight.room.entities.SwitchablesHandler;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.features.actor.Switchable;
import org.ambientlight.room.entities.features.lightobject.Renderable;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffect;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffectFactory;
import org.ambientlight.room.entities.lightobject.effects.transitions.FadeInTransition;
import org.ambientlight.room.entities.lightobject.programms.RenderingProgramm;
import org.ambientlight.room.entities.lightobject.programms.SimpleColor;
import org.ambientlight.room.entities.lightobject.programms.Sunset;
import org.ambientlight.room.entities.lightobject.programms.Tron;


public class LightObjectManager extends Manager implements SwitchablesHandler {

	private RenderingTask renderTask;
	private Timer rendeTimer;

	private final int FREQUENCY;

	private LightObjectManagerConfiguration config;

	private CallBackManager callBackMananger;

	private RenderingEffectFactory effectFactory;

	private Map<EntityId, RenderObject> lightObjectRenderObjects = new HashMap<EntityId, RenderObject>();

	private BufferedImage pixelMap;

	public Renderer renderer;

	public List<AnimateableLedDevice> devices;


	public LightObjectManager(BufferedImage pixelMap, LightObjectManagerConfiguration config,
			RenderingEffectFactory effectFactory, List<AnimateableLedDevice> devices, Renderer renderer,
			CallBackManager callBackMananger, FeatureFacade entitiesFacade, Persistence persistence, int frequency, boolean debug) {

		this.persistence = persistence;
		this.FREQUENCY = frequency;
		this.callBackMananger = callBackMananger;
		this.renderer = renderer;
		this.pixelMap = pixelMap;
		this.effectFactory = effectFactory;
		this.devices = devices;
		this.config = config;

		for (LightObject currentLightObject : config.lightObjects.values()) {

			List<StripePart> stripePartsInLightObject = this.getStripePartsFromRoomForLightObject(getAllStripePartsInRoom(),
					currentLightObject);

			RenderObject currentRenderObject = new RenderObject(currentLightObject, stripePartsInLightObject);

			lightObjectRenderObjects.put(currentLightObject.getId(), currentRenderObject);
			if (currentLightObject.getPowerState()) {
				addLightObjectToRender(renderer, currentRenderObject, effectFactory.getFadeInEffect(currentRenderObject));
			}
		}

		// listen for switchable events
		for (LightObject lightObject : config.lightObjects.values()) {
			entitiesFacade.registerSwitchable(this, lightObject);
		}

		this.rendeTimer = new Timer();
		this.renderTask = new RenderingTask(renderer, this, devices, debug);
		rendeTimer.schedule(renderTask, 0, 1000 / FREQUENCY);
	}


	public void onDestroy() {
		this.rendeTimer.cancel();
		this.renderTask.debugRoomDisplay.dispose();
		for (AnimateableLedDevice current : this.devices) {
			current.closeConnection();
		}
	}


	public BufferedImage getRoomBitMap() {
		return pixelMap;
	}


	public void setRoomBitMap(BufferedImage roomBitMap) {
		this.pixelMap = roomBitMap;
	}


	private void addLightObjectToRender(Renderer renderer, RenderObject lightObject, FadeInTransition transition) {

		if (lightObject.lightObject.getPowerState() == false)
			return;

		RenderingProgramm renderProgram = null;

		// create SimpleColor
		if (lightObject.lightObject.getRenderingProgrammConfiguration() instanceof SimpleColorRenderingProgramConfiguration) {
			SimpleColorRenderingProgramConfiguration config = (SimpleColorRenderingProgramConfiguration) lightObject.lightObject
					.getRenderingProgrammConfiguration();
			Color simpleColor = new Color(config.rgb);
			renderProgram = new SimpleColor(simpleColor);
		}

		// create Tron
		if (lightObject.lightObject.getRenderingProgrammConfiguration() instanceof TronRenderingProgrammConfiguration) {
			TronRenderingProgrammConfiguration config = (TronRenderingProgrammConfiguration) lightObject.lightObject
					.getRenderingProgrammConfiguration();
			Color color = new Color(config.rgb);
			renderProgram = new Tron(lightObject, color, config.lightImpact, config.tailLength, config.sparkleStrength,
					config.sparkleSize, config.speed, config.lightPointAmount);
		}

		// create Sunset
		if (lightObject.lightObject.getRenderingProgrammConfiguration() instanceof SunSetRenderingProgrammConfiguration) {
			SunSetRenderingProgrammConfiguration config = (SunSetRenderingProgrammConfiguration) lightObject.lightObject
					.getRenderingProgrammConfiguration();
			renderProgram = new Sunset(config.duration, config.position, config.sunStartX, config.sunStartY, config.sunSetX,
					config.sizeOfSun, config.gamma, FREQUENCY);
		}

		renderProgram.addEffect(transition);

		renderer.addRenderTaskForLightObject(lightObject, renderProgram);
	}


	public void setRenderingConfiguration(RenderingProgramConfiguration newConfig, EntityId id) {

		System.out.println("LightObjectManager setRenderingConfiguration: setting renderingConfig: "
				+ newConfig.getClass().getSimpleName() + " for id: " + id);

		RenderObject renderObject = lightObjectRenderObjects.get(id);

		if (renderObject == null) {
			System.out.println("LightObjectManager setRenderingConfiguration: unknown renderable Id: " + id);
			return;
		}

		persistence.beginTransaction();

		renderObject.lightObject.setRenderingProgrammConfiguration(newConfig);

		persistence.commitTransaction();
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


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.SwitchablesHandler#setPowerState(java. lang.String,
	 * org.ambientlight.room.entities.switches.SwitchType, boolean)
	 */
	@Override
	public void setPowerState(EntityId id, boolean powerState, boolean fireEvent) {

		RenderObject renderObject = lightObjectRenderObjects.get(id);

		if (renderObject == null) {
			System.out.println("LightObjectManager - setPowerState(): lightObject with ID does not exist: " + id);
			return;
		}

		if (renderObject.lightObject.getPowerState() == powerState) {
			System.out.println("LightObjectManager - setPowerState(): lightObject" + renderObject.lightObject.getId()
					+ " already set to: "
					+ powerState);
			return;
		}

		persistence.beginTransaction();
		renderObject.lightObject.setPowerState(powerState);
		persistence.commitTransaction();
		callBackMananger.roomConfigurationChanged();

		if (powerState == false) {
			// set fadeout effect
			RenderingEffect effect = effectFactory.getFadeOutEffect(renderObject);
			RenderingProgramm renderProgram = renderer.getProgramForLightObject(renderObject);
			renderProgram.addEffect(effect);
			renderer.addRenderTaskForLightObject(renderObject, renderProgram);

		} else {
			this.addLightObjectToRender(renderer, renderObject, effectFactory.getFadeInEffect(renderObject));
		}
	}


	public List<StripePart> getAllStripePartsInRoom() {
		List<StripePart> result = new ArrayList<StripePart>();
		for (DeviceDriver currentDevice : devices) {
			if (currentDevice instanceof LedStripeDeviceDriver) {
				LedStripeDeviceDriver currentLedStripeDevice = (LedStripeDeviceDriver) currentDevice;
				for (Stripe currentStripe : currentLedStripeDevice.getAllStripes()) {
					result.addAll(currentStripe.getStripeParts());
				}
			}
		}
		return result;
	}


	public List<LedPoint> getAllLedPointsInRoom() {
		List<LedPoint> result = new ArrayList<LedPoint>();
		for (DeviceDriver currentDevice : devices) {
			if (currentDevice instanceof LedPointDeviceDriver) {
				LedPointDeviceDriver currentLedPointDevice = (LedPointDeviceDriver) currentDevice;
				result.addAll(currentLedPointDevice.getLedPoints());
			}
		}
		return result;
	}


	public BufferedImage getPixelMap() {
		return pixelMap;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.SwitchablesHandler#getSwitchable(org.
	 * ambientlight.room.entities.features.actor.types.SwitchableId)
	 */
	@Override
	public Switchable getSwitchable(EntityId id) {
		return config.lightObjects.get(id);
	}


	public Set<EntityId> getRenderables() {
		return config.lightObjects.keySet();
	}


	public Renderable getRenderable(EntityId id) {
		return config.lightObjects.get(id);
	}
}
