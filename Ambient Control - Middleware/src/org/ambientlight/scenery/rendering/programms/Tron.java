package org.ambientlight.scenery.rendering.programms;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.entities.StripePart;


/*
 * list of all lanes within a lightobject. no crosses within a lane are allowed. each lane has only one startpoint and one endpoint
 * startx starty vs. endx and endy
 * for cross calculation of a stripe a pixel threshold has to be defined
 * there is a list of crosses -> every cross contains a number of lanes. info wether its the start or the end, and has to deliver information how much the moving direction is changed in degrees.
 * 
 * there is a defined amount of lightpoint. they have a glow length and a speed, they will move with a given speed number. 
 * they try move on crosses with an max given angle. 
 * after a couple of cylcles the lightpoint dies and a new one will be created instead.
 */
public class Tron extends RenderingProgramm {

	int speed=1;
	int positionThreshold=1;
	int knotsPerLane;
	
	@Override
	public BufferedImage renderLightObject(LightObject lightObject) {
		Map<String,List<StripePart>> lanes = new HashMap<String, List<StripePart>>();
		List<StripePart> usedStripeParts = new ArrayList<StripePart>();
		
		int foundKnots=0;
		for (StripePart current: lightObject.stripeParts){
			if(usedStripeParts.contains(current)){
				continue;
			}
			usedStripeParts.add(current);
			//find other at the end or the beginning
//			StripePart nextInLane = this.findNextStripepartInLane(current, lightObject.stripeParts, usedStripeParts);
		}
		
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

//	private StripePart findNextStripepartInLane(StripePart current,
//			List<StripePart> stripeParts, List<StripePart> usedStripeParts) {
//		int x=current.configuration.startXPositionInRoom;
//		int y=current.configuration.startYPositionInRoom;
//		
//		for(StripePart possibleCandidate :stripeParts){
//			if(withinThreshold(possibleCandidate.configuration.startXPositionInRoom,current.configuration.startXPositionInRoom)
//					&&possibleCandidate.configuration.startYPositionInRoom,current.configuration.startYPositionInRoom)){
//						
//					}
//		}
//		return null;
//	}

	
	
	private boolean withinThreshold(int pos1, int pos2){
		if (pos1 >pos2){
			return (pos1-pos2<=positionThreshold? true : false);
		}
		else{
			return (pos2-pos1<=positionThreshold? true : false);
		}
		
	}

	@Override
	public boolean hasDirtyRegion() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
