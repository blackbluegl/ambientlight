package org.ambientlight.scenery.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ambientlight.room.entities.LightObject;
import org.ambientlight.room.entities.Room;
import org.ambientlight.scenery.rendering.effects.RenderingEffect;
import org.ambientlight.scenery.rendering.effects.transitions.FadeOutTransition;
import org.ambientlight.scenery.rendering.effects.transitions.Transition;
import org.ambientlight.scenery.rendering.programms.ITransitionEffectFinishedListener;
import org.ambientlight.scenery.rendering.programms.RenderingProgramm;
import org.ambientlight.scenery.rendering.util.ImageUtil;
import org.ambientlight.scenery.rendering.util.StripePixelMapping;
import org.ambientlight.scenery.rendering.util.StripeUtil;


public class Renderer {

	boolean debug = false;

	Room room;

	ITransitionEffectFinishedListener transitionFinishedListener;

	List<StripePixelMapping> stripePixelMapping = new ArrayList<StripePixelMapping>();

	boolean hadDirtyRegionInLastRun = false;


	public boolean hadDirtyRegionInLastRun() {
		return hadDirtyRegionInLastRun;
	}

	private Map<LightObject, RenderingProgramm> renderLightObjectMapping = new ConcurrentHashMap<LightObject, RenderingProgramm>();


	public Renderer(Room room, ITransitionEffectFinishedListener transitionFinishedListener) {
		this.room = room;
		this.transitionFinishedListener = transitionFinishedListener;

		BufferedImage roomCanvas = room.getRoomBitMap();
		// reset color of background to black
		roomCanvas = ImageUtil.getPaintedImage(roomCanvas,Color.black);
	}


	public void setRenderTaskForLightObject(LightObject lightObject, RenderingProgramm programm) {
		this.renderLightObjectMapping.put(lightObject, programm);
		this.stripePixelMapping = StripeUtil.getStripePixelMapping(room.getAllStripePartsInRoom());
	}


	public void removeRenderTaskForLightObject(LightObject lightObject) {
		renderLightObjectMapping.remove(lightObject);
		this.stripePixelMapping = StripeUtil.getStripePixelMapping(room.getAllStripePartsInRoom());
	}


	public RenderingProgramm getProgramForLightObject(LightObject lightObject) {
		return renderLightObjectMapping.get(lightObject);
	}


	public void render() {

		this.hadDirtyRegionInLastRun = false;

		this.mergeLightObjectsToRoomCanvas();
		if (this.hadDirtyRegionInLastRun) {
			this.fillStripesWithLight();
		}
	}


	private void mergeLightObjectsToRoomCanvas() {
		BufferedImage roomCanvas = room.getRoomBitMap();
		// reset color of background to black
		roomCanvas = ImageUtil.getPaintedImage(roomCanvas,Color.black);

		// boolean imageReseted = false;

		Set<LightObject> lightObjects = renderLightObjectMapping.keySet();

		int maxLayer = 0;
		for (LightObject current : lightObjects) {
			if (current.getConfiguration().layerNumber > maxLayer) {
				maxLayer = current.getConfiguration().layerNumber;
			}
		}

		int currentLayer = 0;
		while (currentLayer <= maxLayer) {
			// iterate through all objects and look if the layer is the current
			// layer to merge
			for (LightObject currentLightObject : lightObjects) {

				if (currentLightObject.getConfiguration().layerNumber == currentLayer) {
					// TODO this here could be done in several threads

					BufferedImage result = renderLightObjectCanvas(currentLightObject);

					// merge to room
					if (result != null) {
						Graphics2D g2d = room.getRoomBitMap().createGraphics();
						g2d.drawImage(result, null, currentLightObject.getConfiguration().xOffsetInRoom,
								currentLightObject.getConfiguration().yOffsetInRoom);
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
			if (effect instanceof FadeOutTransition) {
				return null;
			}
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
		BufferedImage background = room.getRoomBitMap().getSubimage(lightObject.getConfiguration().xOffsetInRoom,
				lightObject.getConfiguration().yOffsetInRoom, lightObject.getConfiguration().width,
				lightObject.getConfiguration().height);
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
	}
}
