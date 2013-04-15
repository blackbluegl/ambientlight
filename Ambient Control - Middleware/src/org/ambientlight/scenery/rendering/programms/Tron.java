package org.ambientlight.scenery.rendering.programms;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.device.stripe.StripePartConfiguration;
import org.ambientlight.scenery.entities.LightObject;
import org.ambientlight.scenery.entities.StripePart;
import org.ambientlight.scenery.rendering.util.ImageUtil;
import org.ambientlight.scenery.rendering.util.StripeUtil;


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

	int speed = 1;
	int tokensAmount = 3;

	int lightObjectXOffset=0;
	int lightObjectYOffset=0;
	
	private class CrossWay {

		int xPosition;
		int yPosition;
		List<StripePartConfiguration> stripeParts = new ArrayList<StripePartConfiguration>();
	}

	private class Token {

		public class Velocity {

			double xVelocity;
			double yVelocity;
			double distancePerStep;
			double distanceTakenInLane;
		}

		Velocity velocity;
		double xPosition;
		double yPosition;
		StripePartConfiguration myCurrentLane;
		int lifetime;
		boolean moveForward;


		public Token(StripePartConfiguration stripePart, double speed) {
			myCurrentLane = stripePart;
			this.velocity = getVelocityForToken(stripePart, speed);
		}


		public Velocity getVelocityForToken(StripePartConfiguration stripe, double speed) {
			double angle = Math.atan2(stripe.endYPositionInRoom - stripe.startYPositionInRoom, stripe.endXPositionInRoom
					- stripe.startXPositionInRoom);
			double xVel = speed * Math.cos(angle);
			double yVel = speed * Math.sin(angle);

			Velocity v = new Velocity();
			v.xVelocity = xVel;
			v.yVelocity = yVel;

			v.distancePerStep = Math.sqrt(((xPosition + v.xVelocity) - xPosition) * (xPosition + v.xVelocity - xPosition)
					+ (yPosition + v.yVelocity - yPosition) * (yPosition + v.yVelocity - yPosition));
			return v;
		}
	}

	List<StripePartConfiguration> lanes = new ArrayList<StripePartConfiguration>();
	List<Token> tokens = new ArrayList<Tron.Token>();
	List<CrossWay> crossWays = new ArrayList<Tron.CrossWay>();


	public Tron(LightObject lightObject, int speed, int tokensAmount) {
		
		this.lightObjectXOffset=lightObject.getConfiguration().xOffsetInRoom;
		this.lightObjectYOffset=lightObject.getConfiguration().yOffsetInRoom;
		
		this.speed=speed;
		this.tokensAmount=tokensAmount;
		List<StripePart> allStripeParts = AmbientControlMW.getRoom().getAllStripePartsInRoom();

		// determine stripeParts within lightobject

		for (StripePart currentStripePart : allStripeParts) {
			if (StripeUtil.isStripePartInLightObject(currentStripePart, lightObject.getConfiguration())) {
				this.lanes.add(currentStripePart.configuration);
			}
		}

		// find crossways out of the given stripeparts - walk through stripe and
		// search for crossways

		for (StripePartConfiguration stripePartToWalkThrough : lanes) {
			for (StripePartConfiguration stripePartToCompareWith : lanes) {
				if (stripePartToWalkThrough == stripePartToCompareWith) {
					continue;
				}
				if (doStripesXCross(stripePartToWalkThrough, stripePartToCompareWith) == false) {
					continue;
				}
				CrossWay possibleCrossWay = this.calculateCrossWay(stripePartToWalkThrough,
						stripePartToCompareWith);
				// the crossway may exist already. if so check the
				// stripePartConfigs and add missing
				boolean crossWayAlreadyExists = false;
				for (CrossWay crossWayToTest : crossWays) {
					if (crossWayToTest.xPosition == possibleCrossWay.xPosition
							&& crossWayToTest.yPosition == possibleCrossWay.yPosition) {
						for(StripePartConfiguration possibleToAdd : possibleCrossWay.stripeParts){
							if(crossWayToTest.stripeParts.contains(possibleToAdd)== false){
								crossWayToTest.stripeParts.add(possibleToAdd);
							}
						}
						crossWayAlreadyExists = true;
						break;
					}
				}
				if (crossWayAlreadyExists == false) {
					crossWays.add(possibleCrossWay);
				}
			}
		}
	}


	private CrossWay calculateCrossWay(StripePartConfiguration configuration, StripePartConfiguration configuration2) {
		Point position = intersection(configuration.startXPositionInRoom, configuration.startYPositionInRoom,
				configuration.endXPositionInRoom, configuration.endYPositionInRoom, configuration2.startXPositionInRoom,
				configuration2.startYPositionInRoom, configuration2.endXPositionInRoom, configuration2.endYPositionInRoom);
		CrossWay crossWay = new CrossWay();
		crossWay.stripeParts.add(configuration);
		crossWay.stripeParts.add(configuration2);
		crossWay.xPosition = position.x;
		crossWay.yPosition = position.y;
		return crossWay;
	}


	@Override
	public BufferedImage renderLightObject(LightObject lightObject) {
		
		handleTokens();
		for(Token current : tokens){
			current.lifetime--;
		}
		
		renderLanes(lightObject.getPixelMap());
		
		return lightObject.getPixelMap();
	}


	private void renderLanes(BufferedImage pixelMap) {
		pixelMap = ImageUtil.getBlackImage(pixelMap);
		Graphics g = pixelMap.getGraphics();
		for(Token token : tokens){
			g.setColor(Color.RED);
			g.drawOval((int)token.xPosition-this.lightObjectXOffset, (int)token.yPosition-this.lightObjectYOffset, 3, 3);	
		}
	}


	private void handleTokens() {
		// delete dead tokens
		List<Token> tokensToRemove = new ArrayList<Tron.Token>();
		for (Token current : tokens) {
			if (current.lifetime == 0 || current.velocity.distanceTakenInLane>= getLengthOfLane(current.myCurrentLane)) {
				tokensToRemove.add(current);
			}
		}
		tokens.removeAll(tokensToRemove);

		// create new Tokens to keep amount stable
		for (int i = tokens.size(); tokens.size() < tokensAmount; i++) {
			tokens.add(createNewToken());
		}
		
		//now move Token
		for(Token current : tokens){
			moveToken(current);
		}

	}


	private void moveToken(Token token) {
		// is a cross within one step reachable?
		for (CrossWay possibleCrossWay : crossWays) {
			if (possibleCrossWay.stripeParts.contains(token.myCurrentLane)) {
				if (this.getDistance(token.xPosition, possibleCrossWay.xPosition, token.yPosition, possibleCrossWay.yPosition) <= token.velocity.distancePerStep) {
					// do we want to take that cross?
					if (Math.random() > 0.5) {
						// yes case
						if (handleTokenTakesCrossway(token, possibleCrossWay) == false) {
							continue; // we wanted to take an new lane but there
										// was not enough space on it. so it
										// does not make sence
						}
						return;
					} else {
						// do nothing here there might be other crossways
						// reachable to take.
					}
				}
			}
		}
		// if there where no crossways available or we did not chose to take
		// one. just move on
		
		double velocityX = token.velocity.xVelocity;
		double velocityY = token.velocity.yVelocity;
		if(token.moveForward==false){
			velocityX=-velocityX;
			velocityY=-velocityY;
		}
		token.xPosition=token.xPosition+velocityX;
		token.yPosition=token.yPosition+velocityY;

		token.velocity.distanceTakenInLane = token.velocity.distanceTakenInLane+token.velocity.distancePerStep;
	}


	private boolean handleTokenTakesCrossway(Token token, CrossWay crossWay) {
		//calculate the distance from token to crossway. we need this value later on several times.
		double distanceToCrossway= token.velocity.distancePerStep -
				this.getDistance(token.xPosition, crossWay.xPosition, token.yPosition, crossWay.yPosition);
		
		//decide which lane to take. there might be more than one. if possible 
		Map<StripePartConfiguration,Boolean> lanesReadyToTake = new HashMap<StripePartConfiguration, Boolean>();
		for(StripePartConfiguration possibleLane : crossWay.stripeParts){
			if(possibleLane ==token.myCurrentLane){
				continue; //thats the lane we want to leave
			}
			
			boolean forwardPossible = false;
			double lengthFromBeginningToToken = this.getDistance(possibleLane.startXPositionInRoom, crossWay.xPosition, possibleLane.startYPositionInRoom, crossWay.yPosition)+distanceToCrossway;
			if(lengthFromBeginningToToken<getLengthOfLane(possibleLane)){
				forwardPossible = true;
			}
			boolean backwardsPossible = false;
			double lengthFromEndToToken = this.getDistance(possibleLane.endXPositionInRoom, crossWay.xPosition, possibleLane.endYPositionInRoom, crossWay.yPosition)+distanceToCrossway;
			if(lengthFromEndToToken<getLengthOfLane(possibleLane)){
				backwardsPossible = true;
			}
			
			//if we cannot use any direction continue with next cross
			if(forwardPossible == false && backwardsPossible == false){
				continue;
			}
		
			//decide the prefered direction, forward or backward
			boolean forward= Math.random() >0.5 ? true : false;
			if( forward&& forwardPossible == false){
				forward = false;
			}
			if(!forward && backwardsPossible == false){
				forward = true;
			}
			
			lanesReadyToTake.put(possibleLane, forward);
		}
		
		if(lanesReadyToTake.size()==0){
			return false;
		}
		
		//now move the token
		int laneNumberChoosen = (int) (Math.random()*(lanesReadyToTake.size()-1));
		StripePartConfiguration newLane =  (StripePartConfiguration) lanesReadyToTake.keySet().toArray()[laneNumberChoosen];
		
		token.myCurrentLane=newLane;
		token.moveForward=lanesReadyToTake.get(newLane);
		token.xPosition=crossWay.xPosition;
		token.yPosition=crossWay.yPosition;
		
		token.velocity = token.getVelocityForToken(newLane, speed);
		
		double distanceLeftInPercent = 1-(distanceToCrossway/token.velocity.distancePerStep);
		double velocityX = token.velocity.xVelocity*distanceLeftInPercent;
		double velocityY = token.velocity.yVelocity*distanceLeftInPercent;
		if(token.moveForward==false){
			velocityX=-velocityX;
			velocityY=-velocityY;
		}
		
		token.xPosition=token.xPosition+velocityX;
		token.yPosition=token.yPosition+velocityY;
		
		double startX;
		double startY;
		if(token.moveForward){
			startX = token.myCurrentLane.startXPositionInRoom;
			startY = token.myCurrentLane.startYPositionInRoom;
		}
		else{
			startX = token.myCurrentLane.endXPositionInRoom;
			startY = token.myCurrentLane.endYPositionInRoom;
		}
		double offsetInLane = token.velocity.distancePerStep-distanceToCrossway;
		double distanceTakenInLane = this.getDistance(startX, crossWay.xPosition, startY, crossWay.yPosition)+offsetInLane;
		token.velocity.distanceTakenInLane = distanceTakenInLane;
		
		return true;
	}


	private double getLengthOfLane(StripePartConfiguration myCurrentLane) {
		return this.getDistance(myCurrentLane.startXPositionInRoom, myCurrentLane.endXPositionInRoom, 
				myCurrentLane.startYPositionInRoom, myCurrentLane.endYPositionInRoom);
	}


	private Token createNewToken() {
		// get any lane
		StripePartConfiguration randomStripePart = this.lanes.get((int) ((Math.random() * this.lanes.size())));
		// get randomPosition on that StripePart
		Token result = new Token(randomStripePart, this.speed);

		double maxDistaneInLane = Math.sqrt((randomStripePart.endXPositionInRoom - randomStripePart.startXPositionInRoom)
				* (randomStripePart.endXPositionInRoom - randomStripePart.startXPositionInRoom)
				+ (randomStripePart.endYPositionInRoom - randomStripePart.startYPositionInRoom)
				* (randomStripePart.endYPositionInRoom - randomStripePart.startYPositionInRoom));

		if (maxDistaneInLane <= result.velocity.distancePerStep) {
			result.xPosition = randomStripePart.startXPositionInRoom;
			result.yPosition = randomStripePart.startYPositionInRoom;
		} else {
			double possibleStepsInLane = (maxDistaneInLane / result.velocity.distancePerStep);
			double xOffset = Math.random() * possibleStepsInLane * result.velocity.xVelocity;
			double yOffset = Math.random() * possibleStepsInLane * result.velocity.yVelocity;
			result.xPosition = randomStripePart.startXPositionInRoom + xOffset;
			result.yPosition = randomStripePart.startYPositionInRoom + yOffset;
		}
		result.velocity.distanceTakenInLane = getDistance(randomStripePart.startXPositionInRoom, result.xPosition,
				randomStripePart.startYPositionInRoom, result.yPosition);

		return result;
	}


	private double getDistance(double xS, double xE, double yS, double yE) {
		return Math.sqrt((xE - xS) * (xE - xS) + (yE - yS) * (yE - yS));
	}


	@Override
	public boolean hasDirtyRegion() {
		return true;
	}


	public boolean doStripesXCross(StripePartConfiguration stripe1, StripePartConfiguration stripe2) {
		Line2D.Float line1 = new Line2D.Float(stripe1.startXPositionInRoom, stripe1.startYPositionInRoom,
				stripe1.endXPositionInRoom, stripe1.endYPositionInRoom);
		Line2D.Float line2 = new Line2D.Float(stripe2.startXPositionInRoom, stripe2.startYPositionInRoom,
				stripe2.endXPositionInRoom, stripe2.endYPositionInRoom);
		return line1.intersectsLine(line2);
	}


	/**
	 * Computes the intersection between two lines. The calculated point is
	 * approximate, since integers are used. If you need a more precise result,
	 * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
	 * license). http://www.ahristov.com
	 * 
	 * @param x1
	 *            Point 1 of Line 1
	 * @param y1
	 *            Point 1 of Line 1
	 * @param x2
	 *            Point 2 of Line 1
	 * @param y2
	 *            Point 2 of Line 1
	 * @param x3
	 *            Point 1 of Line 2
	 * @param y3
	 *            Point 1 of Line 2
	 * @param x4
	 *            Point 2 of Line 2
	 * @param y4
	 *            Point 2 of Line 2
	 * @return Point where the segments intersect, or null if they don't
	 */
	private Point intersection(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		int d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d == 0)
			return null;

		int xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
		int yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

		return new Point(xi, yi);
	}

}
