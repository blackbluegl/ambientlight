package org.ambientlight.scenery.rendering;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.entities.Room;
import org.ambientlight.scenery.entities.StripePart;
import org.ambientlight.scenery.rendering.effects.RenderingEffect;
import org.ambientlight.scenery.rendering.effects.transitions.FadeOutTransition;
import org.ambientlight.scenery.rendering.effects.transitions.Transition;
import org.ambientlight.scenery.rendering.programms.ITransitionEffectFinishedListener;
import org.ambientlight.scenery.rendering.programms.RenderingProgramm;
import org.ambientlight.scenery.rendering.util.ImageUtil;

public class Renderer {
	
	boolean debug = false;
	
	Room room;

	ITransitionEffectFinishedListener transitionFinishedListener;

	List<StripePixelMapping> stripePixelMapping = new ArrayList<StripePixelMapping>();

	boolean hadDirtyRegionInLastRun = false;
	
	public boolean hadDirtyRegionInLastRun() {
		return hadDirtyRegionInLastRun;
	}


	class StripePixelMapping {

		public StripePixelMapping(int xPosition, int yPosition,
				int stripeModellPosition, StripePart stripeModell) {
			this.xPosition = xPosition;
			this.yPosition = yPosition;
			this.stripePartPosition = stripeModellPosition;
			this.stripePart = stripeModell;
		}

		int xPosition;
		int yPosition;
		int stripePartPosition;
		StripePart stripePart;
	}

	
	private Map<LightObject, RenderingProgramm> renderLightObjectMapping = new ConcurrentHashMap<LightObject, RenderingProgramm>();

	
	public Renderer(Room room,
			ITransitionEffectFinishedListener transitionFinishedListener) {
		this.room = room;
		this.transitionFinishedListener = transitionFinishedListener;
		
		BufferedImage roomCanvas = room.getRoomBitMap();
		//reset color of background to black
		roomCanvas = ImageUtil.getBlackImage(roomCanvas);
	}

	
	public  void setRenderTaskForLightObject(LightObject lightObject,
			RenderingProgramm programm) {
		this.renderLightObjectMapping.put(lightObject, programm);
		this.stripePixelMapping = this.getStripePixelMapping(room
				.getAllStripePartsInRoom());
	}

	public  void removeRenderTaskForLightObject(LightObject lightObject) {
		renderLightObjectMapping.remove(lightObject);
		this.stripePixelMapping = this.getStripePixelMapping(room
				.getAllStripePartsInRoom());
	}

	public  RenderingProgramm getProgramForLightObject(LightObject lightObject) {
		return renderLightObjectMapping.get(lightObject);
	}

	
	public void render() {

		this.hadDirtyRegionInLastRun=false;	
		
		this.mergeLightObjectsToRoomCanvas();
		if (this.hadDirtyRegionInLastRun) {
			this.fillStripesWithLight();
		}
	}

	
	private void mergeLightObjectsToRoomCanvas() {		
		BufferedImage roomCanvas = room.getRoomBitMap();
		//reset color of background to black
		roomCanvas = ImageUtil.getBlackImage(roomCanvas);
		
//		boolean imageReseted = false;
		
		Set<LightObject> lightObjects = renderLightObjectMapping.keySet();

		int maxLayer=0;
		for(LightObject current : lightObjects){
			if(current.getConfiguration().layerNumber>maxLayer){
				maxLayer=current.getConfiguration().layerNumber;
			}
		}
		
		int currentLayer = 0;
		while (currentLayer<=maxLayer) {
			// iterate through all objects and look if the layer is the current
			// layer to merge
			for (LightObject currentLightObject : lightObjects) {

				if (currentLightObject.getConfiguration().layerNumber == currentLayer) {
					// TODO this here could be done in several threads

					BufferedImage result = renderLightObjectCanvas(currentLightObject);
					
					// merge to room
						if (result != null) {
							Graphics2D g2d = room.getRoomBitMap().createGraphics();
							g2d.drawImage(result,null,
									currentLightObject.getConfiguration().xOffsetInRoom,
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
		RenderingProgramm currentRenderProgramm = this.renderLightObjectMapping
				.get(lightObject);
		RenderingEffect effect = currentRenderProgramm.getEffect();

		// handle finished transitions
		if (effect != null && effect instanceof Transition && ((Transition) effect).isFinished() == true){
			this.hadDirtyRegionInLastRun=true;
			this.transitionFinishedListener
			.lightObjectTransitionEffectFinished(this, lightObject);
			currentRenderProgramm.removeEffect();
			
			//after the last step of a fadeout the background should be rendered. 
			if(effect instanceof FadeOutTransition) {
				return null;	
			}
		}
		
		//if no effect exists (anymore) render in quick mode
		if(effect == null){
			BufferedImage result = currentRenderProgramm.renderLightObject(lightObject);
			if(currentRenderProgramm.hasDirtyRegion()==true){
				lightObject.setPixelMapAfterEffect(result);
				this.hadDirtyRegionInLastRun=true;
			}
			return result;
		}


		// init effect before the lightobject value was changed
		BufferedImage background = room.getRoomBitMap().getSubimage(
				lightObject.getConfiguration().xOffsetInRoom,
				lightObject.getConfiguration().yOffsetInRoom,
				lightObject.getConfiguration().width,
				lightObject.getConfiguration().height);
		effect.beforeRendering(background, lightObject.getPixelMap());
		// render the lightObjectvalue
		currentRenderProgramm.renderLightObject(lightObject);
		
		//apply effect
		BufferedImage result = effect.afterRendering(lightObject.getPixelMap());
		lightObject.setPixelMapAfterEffect(result);
		
		//return result to roomCanvas
		this.hadDirtyRegionInLastRun=true;
		return result;
	}

	
	// put to own class
	private List<StripePixelMapping> getStripePixelMapping(
			List<StripePart> stripeParts) {

		List<StripePixelMapping> result = new ArrayList<Renderer.StripePixelMapping>();

		for (StripePart current : stripeParts) {
			Float pixelDifferenceX = (float) ((current.configuration.endXPositionInRoom - current.configuration.startXPositionInRoom) / (current.configuration.pixelAmount-1));
			Float pixelDifferenceY = (float) ((current.configuration.endYPositionInRoom - current.configuration.startYPositionInRoom) / (current.configuration.pixelAmount-1));

			for (int i = 0; i < current.configuration.pixelAmount; i++) {
				float xPosition = current.configuration.startXPositionInRoom
						+ (i * pixelDifferenceX);
				float yPosition = current.configuration.startYPositionInRoom
						+ (i * pixelDifferenceY);
				result.add(new StripePixelMapping((int) xPosition,
						(int) yPosition, i , current));
			}
		}
		return result;
	}

	
	// put to stripemapper
	public void fillStripesWithLight() {
			for (StripePixelMapping current : this.stripePixelMapping) {
				int rgbValue = room.getRoomBitMap().getRGB(current.xPosition,
						current.yPosition);
				current.stripePart.setPixelData(current.stripePartPosition,
						rgbValue);
			}
	}
}
