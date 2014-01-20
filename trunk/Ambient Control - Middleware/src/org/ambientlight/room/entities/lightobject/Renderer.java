package org.ambientlight.room.entities.lightobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.ambientlight.device.led.LedPoint;
import org.ambientlight.device.led.StripePart;
import org.ambientlight.room.entities.lightobject.effects.RenderingEffect;
import org.ambientlight.room.entities.lightobject.effects.transitions.FadeOutTransition;
import org.ambientlight.room.entities.lightobject.effects.transitions.Transition;
import org.ambientlight.room.entities.lightobject.programms.RenderingProgramm;
import org.ambientlight.room.entities.lightobject.util.ImageUtil;
import org.ambientlight.room.entities.lightobject.util.StripePixelMapping;
import org.ambientlight.room.entities.lightobject.util.StripeUtil;


public class Renderer {

	List<LightObject> queueDeleteLightObjects = new ArrayList<LightObject>();

	boolean debug = false;

	List<StripePixelMapping> stripePixelMapping = new ArrayList<StripePixelMapping>();
	List<LedPoint> ledPoints = new ArrayList<LedPoint>();

	boolean hadDirtyRegionInLastRun = false;

	private final ReentrantLock lightObjectMappingLock = new ReentrantLock();

	BufferedImage roomCanvas;


	public boolean hadDirtyRegionInLastRun() {
		return hadDirtyRegionInLastRun;
	}

	private final Map<LightObject, RenderingProgramm> renderLightObjectMapping = new HashMap<LightObject, RenderingProgramm>();


	public Renderer(BufferedImage roomBitMap, List<StripePart> allStripeParts, List<LedPoint> allLedPoints) {

		roomCanvas = roomBitMap;
		// reset color of background to black
		roomCanvas = ImageUtil.getPaintedImage(roomCanvas, Color.black);

		this.stripePixelMapping = StripeUtil.getStripePixelMapping(allStripeParts);
		this.ledPoints = allLedPoints;
	}


	public void addRenderTaskForLightObject(LightObject lightObject, RenderingProgramm programm) {
		try {
			lightObjectMappingLock.lock();
			renderLightObjectMapping.put(lightObject, programm);
		} finally {
			lightObjectMappingLock.unlock();
		}
	}


	public synchronized void removeRenderTaskForLightObject(LightObject lightObject) {
		try {
			lightObjectMappingLock.lock();
			renderLightObjectMapping.remove(lightObject);
		} finally {
			lightObjectMappingLock.unlock();
		}
	}


	public RenderingProgramm getProgramForLightObject(LightObject lightObject) {
		return renderLightObjectMapping.get(lightObject);
	}


	public void render() {
		try {
			lightObjectMappingLock.lock();

			this.hadDirtyRegionInLastRun = false;

			this.mergeLightObjectsToRoomCanvas();
			if (this.hadDirtyRegionInLastRun) {
				this.fillStripesWithLight();
			}

			for (LightObject current : this.queueDeleteLightObjects) {
				this.removeRenderTaskForLightObject(current);
			}
			this.queueDeleteLightObjects.clear();

		} finally {
			lightObjectMappingLock.unlock();
		}
	}


	private void mergeLightObjectsToRoomCanvas() {

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
			Iterator<LightObject> lightObjectIterator = lightObjects.iterator();
			while (lightObjectIterator.hasNext()) {
				LightObject currentLightObject = lightObjectIterator.next();

				if (currentLightObject.configuration.layerNumber == currentLayer) {

					BufferedImage result = null;
					try {
						result = renderLightObjectCanvas(currentLightObject);
					} catch (Exception e) {
						// ignore the lightobject
						System.out.println("Error while rendering lightobject: ");
						e.printStackTrace();
					}

					// merge to room
					if (result != null) {
						Graphics2D g2d = roomCanvas.createGraphics();
						g2d.drawImage(result, null, currentLightObject.configuration.xOffsetInRoom,
								currentLightObject.configuration.yOffsetInRoom);
						g2d.dispose();
					}
					else{
						lightObjectIterator.remove();
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
		BufferedImage background = roomCanvas.getSubimage(lightObject.configuration.xOffsetInRoom,
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


	public void fillStripesWithLight() {
		for (StripePixelMapping current : this.stripePixelMapping) {
			int rgbValue = roomCanvas.getRGB(current.xPosition, current.yPosition);
			current.stripePart.setPixelData(current.stripePartPosition, rgbValue);
		}
		for (LedPoint current : this.ledPoints) {
			int rgbValue = roomCanvas.getRGB(current.configuration.xPosition, current.configuration.yPosition);
			current.setPixel(rgbValue);
		}
	}
}
