package org.ambientlight.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ambientlight.device.led.LedPoint;
import org.ambientlight.rendering.effects.RenderingEffect;
import org.ambientlight.rendering.effects.transitions.FadeOutTransition;
import org.ambientlight.rendering.effects.transitions.Transition;
import org.ambientlight.rendering.programms.ITransitionEffectFinishedListener;
import org.ambientlight.rendering.programms.RenderingProgramm;
import org.ambientlight.rendering.util.ImageUtil;
import org.ambientlight.rendering.util.StripePixelMapping;
import org.ambientlight.rendering.util.StripeUtil;
import org.ambientlight.room.Room;
import org.ambientlight.room.entities.LightObject;


public class Renderer {

	boolean debug = false;

	Room room;

	ITransitionEffectFinishedListener transitionFinishedListener;

	List<StripePixelMapping> stripePixelMapping = new ArrayList<StripePixelMapping>();
	List<LedPoint> ledPoints = new ArrayList<LedPoint>();

	boolean hadDirtyRegionInLastRun = false;

	/**
	 * determines if it is possible to add or remove lightobjects to the
	 * renderer
	 */
	boolean locked = false;


	public boolean hadDirtyRegionInLastRun() {
		return hadDirtyRegionInLastRun;
	}

	private final Map<LightObject, RenderingProgramm> renderLightObjectMapping = new HashMap<LightObject, RenderingProgramm>();

	private final List<LightObject> lightObjectsToRemove = new ArrayList<LightObject>();
	private final Map<LightObject, RenderingProgramm> lightObjectsToAdd = new ConcurrentHashMap<LightObject, RenderingProgramm>();


	public Renderer(Room room, ITransitionEffectFinishedListener transitionFinishedListener) {
		this.room = room;
		this.transitionFinishedListener = transitionFinishedListener;

		BufferedImage roomCanvas = room.getRoomBitMap();
		// reset color of background to black
		roomCanvas = ImageUtil.getPaintedImage(roomCanvas, Color.black);

		this.stripePixelMapping = StripeUtil.getStripePixelMapping(room.getAllStripePartsInRoom());
		this.ledPoints = room.getAllLedPointsInRoom();
	}


	public void addRenderTaskForLightObject(LightObject lightObject, RenderingProgramm programm) {
		while (this.locked) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.lightObjectsToAdd.put(lightObject, programm);
	}


	public void removeRenderTaskForLightObject(LightObject lightObject) {
		while (this.locked) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.lightObjectsToRemove.add(lightObject);
	}


	public RenderingProgramm getProgramForLightObject(LightObject lightObject) {
		return renderLightObjectMapping.get(lightObject);
	}


	public void render() {
		this.locked = true;
		// remove all lightObjects that where added during rendering process
		for (LightObject current : this.lightObjectsToRemove) {
			this.renderLightObjectMapping.remove(current);
		}
		this.lightObjectsToRemove.clear();

		// add all new lightobjects since last run
		this.renderLightObjectMapping.putAll(this.lightObjectsToAdd);
		this.lightObjectsToAdd.clear();
		this.locked = false;

		this.hadDirtyRegionInLastRun = false;

		this.mergeLightObjectsToRoomCanvas();
		if (this.hadDirtyRegionInLastRun) {
			this.fillStripesWithLight();
		}
	}


	private void mergeLightObjectsToRoomCanvas() {
		BufferedImage roomCanvas = room.getRoomBitMap();
		// reset color of background to black
		roomCanvas = ImageUtil.getPaintedImage(roomCanvas, Color.black);

		Set<LightObject> lightObjects = renderLightObjectMapping.keySet();

		int maxLayer = 0;
		for (LightObject current : lightObjects) {
			if (current.configuration.layerNumber > maxLayer) {
				maxLayer = current.configuration.layerNumber;
			}
		}

		int currentLayer = 0;
		while (currentLayer <= maxLayer) {
			// iterate through all objects and look if the layer is the current
			// layer to merge
			for (LightObject currentLightObject : lightObjects) {

				if (currentLightObject.configuration.layerNumber == currentLayer) {
					// TODO this here could be done in several threads

					BufferedImage result = renderLightObjectCanvas(currentLightObject);

					// merge to room
					if (result != null) {
						Graphics2D g2d = room.getRoomBitMap().createGraphics();
						g2d.drawImage(result, null, currentLightObject.configuration.xOffsetInRoom,
								currentLightObject.configuration.yOffsetInRoom);
						g2d.dispose();
					}
				}
			}
			// finished all Objects for this layer. go to next Layer
			currentLayer++;
		}

	}


	private BufferedImage renderLightObjectCanvas(LightObject lightObject) {
		// render here
		RenderingProgramm currentRenderProgramm = this.renderLightObjectMapping.get(lightObject);
		RenderingEffect effect = currentRenderProgramm.getEffect();

		// handle finished transitions
		if (effect != null && effect instanceof Transition && ((Transition) effect).isFinished() == true) {
			this.hadDirtyRegionInLastRun = true;
			this.transitionFinishedListener.lightObjectTransitionEffectFinished(this, lightObject);
			currentRenderProgramm.removeEffect();

			// after the last step of a fadeout the background should be
			// rendered.
			if (effect instanceof FadeOutTransition)
				return null;
		}

		// if no effect exists (anymore) render in quick mode
		if (effect == null) {
			BufferedImage result = currentRenderProgramm.renderLightObject(lightObject);
			if (currentRenderProgramm.hasDirtyRegion() == true) {
				lightObject.setPixelMapAfterEffect(result);
				this.hadDirtyRegionInLastRun = true;
			}
			return result;
		}

		// init effect before the lightobject value was changed
		BufferedImage background = room.getRoomBitMap().getSubimage(lightObject.configuration.xOffsetInRoom,
				lightObject.configuration.yOffsetInRoom, lightObject.configuration.width, lightObject.configuration.height);
		effect.beforeRendering(background, lightObject.getPixelMap());
		// render the lightObjectvalue
		currentRenderProgramm.renderLightObject(lightObject);

		// apply effect
		BufferedImage result = effect.afterRendering(lightObject.getPixelMap());
		lightObject.setPixelMapAfterEffect(result);

		// return result to roomCanvas
		this.hadDirtyRegionInLastRun = true;
		return result;
	}


	// TODO put to stripemapper
	public void fillStripesWithLight() {
		for (StripePixelMapping current : this.stripePixelMapping) {
			int rgbValue = room.getRoomBitMap().getRGB(current.xPosition, current.yPosition);
			current.stripePart.setPixelData(current.stripePartPosition, rgbValue);
		}
		for (LedPoint current : this.ledPoints) {
			int rgbValue = room.getRoomBitMap().getRGB(current.configuration.xPosition, current.configuration.yPosition);
			current.setPixel(rgbValue);
		}
	}
}
