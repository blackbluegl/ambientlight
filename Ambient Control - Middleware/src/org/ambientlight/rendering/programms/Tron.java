package org.ambientlight.rendering.programms;

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
import org.ambientlight.device.led.StripePartConfiguration;
import org.ambientlight.rendering.util.ImageUtil;
import org.ambientlight.rendering.util.StripeUtil;
import org.ambientlight.room.StripePart;
import org.ambientlight.room.entities.LightObject;


public class Tron extends RenderingProgramm {

	int r = 20;
	int g = 20;
	int b = 70;

	double lightImpact = 0.9;
	double tailLength = 0.4;

	double sparkleStrength = 0.2;
	double sparkleSize = 0.1;

	double speed = 1;
	int tokensAmount = 3;

	int lightObjectXOffset = 0;
	int lightObjectYOffset = 0;

	Map<Point, PixelColorMapping> pixelMapping = new HashMap<Point, PixelColorMapping>();
	List<Lane> lanes = new ArrayList<Lane>();
	List<Token> tokens = new ArrayList<Tron.Token>();
	List<CrossWay> crossWays = new ArrayList<Tron.CrossWay>();

	private class PixelColorMapping {

		int r;
		int g;
		int b;
		int lifetime;
	}

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
		List<CrossWay> lasTakenCrossWays = new ArrayList<Tron.CrossWay>();
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

	public class Lane {

		List<Color> pixels;
		StripePartConfiguration stripePart;


		public Lane(StripePartConfiguration stripePart) {
			this.stripePart = stripePart;
			this.pixels = new ArrayList<Color>();
		}
	}


	public Tron(LightObject lightObject, Color color, double lightImpact, double tailLength, double sparkleStrength,
			double sparkleSize, double speed, int tokensAmount) {

		this.r = color.getRed();
		this.g = color.getGreen();
		this.b = color.getBlue();
		this.lightImpact = lightImpact;
		this.tailLength = tailLength;
		this.sparkleStrength = sparkleStrength;
		this.sparkleSize = sparkleSize;
		// this functions helps to map values below 7 to a value slower than 1.
		// on the other a speed of 20 will be mapped to a real speed of 40
		this.speed = (int) Math.sqrt(speed * 8);
		this.tokensAmount = tokensAmount;

		this.lightObjectXOffset = lightObject.configuration.xOffsetInRoom;
		this.lightObjectYOffset = lightObject.configuration.yOffsetInRoom;

		List<StripePart> allStripeParts = AmbientControlMW.getRoom().getAllStripePartsInRoom();

		// determine stripeParts within lightobject

		for (StripePart currentStripePart : allStripeParts) {
			if (StripeUtil.isStripePartInLightObject(currentStripePart, lightObject.configuration)) {
				this.lanes.add(new Lane(currentStripePart.configuration));
			}
		}

		// find crossways out of the given stripeparts - walk through stripe and
		// search for crossways

		for (Lane stripePartToWalkThrough : lanes) {
			for (Lane stripePartToCompareWith : lanes) {
				if (stripePartToWalkThrough == stripePartToCompareWith) {
					continue;
				}
				if (doStripesXCross(stripePartToWalkThrough.stripePart, stripePartToCompareWith.stripePart) == false) {
					continue;
				}
				CrossWay possibleCrossWay = this.calculateCrossWay(stripePartToWalkThrough.stripePart,
						stripePartToCompareWith.stripePart);
				// the crossway may exist already. if so check the
				// stripePartConfigs and add missing
				boolean crossWayAlreadyExists = false;
				for (CrossWay crossWayToTest : crossWays) {
					if (crossWayToTest.xPosition == possibleCrossWay.xPosition
							&& crossWayToTest.yPosition == possibleCrossWay.yPosition) {
						for (StripePartConfiguration possibleToAdd : possibleCrossWay.stripeParts) {
							if (crossWayToTest.stripeParts.contains(possibleToAdd) == false) {
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
		lightObject.setPixelMap(ImageUtil.getPaintedImage(lightObject.getPixelMap(), color));
	}


	private void renderLanes(BufferedImage pixelMap) {

		int lightImpactAbsolut = (int) ((255) * this.lightImpact);

		int lengthOfTailInPixel = (int) (this.tailLength * (pixelMap.getWidth() + pixelMap.getHeight()));

		for (Token token : tokens) {
			Point point = new Point((int) token.xPosition, (int) token.yPosition);
			PixelColorMapping pc = new PixelColorMapping();
			pc.lifetime = lengthOfTailInPixel;
			pc.r = lightImpactAbsolut;
			pc.g = lightImpactAbsolut;
			pc.b = lightImpactAbsolut;
			this.pixelMapping.put(point, pc);
		}

		// kill pixels which have no lifetime anymore left
		List<Point> removePixels = new ArrayList<Point>();
		for (Point p : pixelMapping.keySet()) {
			if (pixelMapping.get(p).lifetime == 0) {
				removePixels.add(p);
			}
		}
		for (Point p : removePixels) {
			pixelMapping.remove(p);
		}

		// draw the pixels on background
		for (Point p : this.pixelMapping.keySet()) {
			PixelColorMapping pc = this.pixelMapping.get(p);

			double sparkleLength = this.sparkleSize * (lengthOfTailInPixel);

			double sparcle = this.sparkleStrength * Math.random();
			if (Math.random() > 0.5) {
				sparcle = -sparcle;
			}

			pc.r = this.getColorValue(this.r + lightImpactAbsolut, this.r, pc.lifetime, sparkleLength, lengthOfTailInPixel,
					sparcle);
			pc.g = this.getColorValue(this.g + lightImpactAbsolut, this.g, pc.lifetime, sparkleLength, lengthOfTailInPixel,
					sparcle);
			pc.b = this.getColorValue(this.b + lightImpactAbsolut, this.b, pc.lifetime, sparkleLength, lengthOfTailInPixel,
					sparcle);

			if (speed > 1 && pc.lifetime % (int) this.speed == 0) {
				drawOnPixelMap(pixelMap, p, pc);
			} else if (speed <= 1) {
				drawOnPixelMap(pixelMap, p, pc);
			}
			pc.lifetime--;
		}
	}


	private void drawOnPixelMap(BufferedImage pixelMap, Point p, PixelColorMapping pc) {
		Graphics g = pixelMap.getGraphics();
		Color drawColor = new Color(pc.r, pc.g, pc.b);
		g.setColor(drawColor);
		g.fillRect(p.x - this.lightObjectXOffset, p.y - this.lightObjectYOffset, 2, 2);
	}


	private int getColorValue(int lightPointColor, int backgroundColor, int lifetime, double sparkleSize,
			int lengthOfTailInPixel, double sparcle) {
		int colorValue = 0;
		if (lifetime >= lengthOfTailInPixel - sparkleSize && lifetime > 1) {
			double lightDifference = lightPointColor + (lightPointColor * sparcle);
			colorValue = (int) lightDifference;
		} else if (lifetime <= 1)
			return backgroundColor;
		else {
			double impactCoefficent = (double) lifetime / lengthOfTailInPixel;
			// TODO use negative part of e function. dont forget to use
			// fadeoutrate. or remove it!
			colorValue = backgroundColor + (int) (impactCoefficent * lightPointColor);
			colorValue = (int) (colorValue + (colorValue * sparcle));
		}
		if (colorValue > 255) {
			colorValue = 255;
		}
		if (colorValue < 0) {
			colorValue = 0;
		}
		return colorValue;
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
		for (int i = 0; i < this.speed; i++) {
			handleTokens();
			renderLanes(lightObject.getPixelMap());
		}
		return lightObject.getPixelMap();
	}


	private void handleTokens() {
		// delete dead tokens
		List<Token> tokensToRemove = new ArrayList<Tron.Token>();
		for (Token current : tokens) {
			if (current.lifetime == 0 || current.velocity.distanceTakenInLane >= getLengthOfLane(current.myCurrentLane)) {
				tokensToRemove.add(current);
			}
		}
		tokens.removeAll(tokensToRemove);

		// create new Tokens to keep amount stable
		while (tokens.size() < tokensAmount) {
			// if speed is greater 1 we speed up the rendering cycles. so we get
			// shure that never ever one pixel on the rendered image is missed
			// out.
			if (this.speed < 1) {
				tokens.add(createNewToken(this.speed));
			} else {
				tokens.add(createNewToken(1));
			}
		}

		// now move Token
		for (Token current : tokens) {
			moveToken(current);
		}

		for (Token current : tokens) {
			current.lifetime--;
		}
	}


	private void moveToken(Token token) {
		// is a cross within one step reachable?
		for (CrossWay possibleCrossWay : crossWays) {
			if (possibleCrossWay.stripeParts.contains(token.myCurrentLane) == false) {
				continue;
			}

			if (this.getDistance(token.xPosition, possibleCrossWay.xPosition, token.yPosition, possibleCrossWay.yPosition) > token.velocity.distancePerStep) {
				continue;
			}

			// is that cross behind us? if yes continue. maybe we left it out in
			// last step or took this cross already
			if (isCrossWayBehindToken(token, possibleCrossWay)) {
				continue;
			}

			// remember not to take that cross again in next step.
			token.lasTakenCrossWays.add(possibleCrossWay);

			// do we want to take that cross?
			if (Math.random() > 0.5) {
				// yes case
				if (handleTokenTakesCrossway(token, possibleCrossWay) == false) {
					continue; // we wanted to take an new lane but there
					// was not enough space on it. so it
					// does not make sence
				} else
					return;
			}
		}

		token.lasTakenCrossWays.clear();
		// if there where no crossways available or we did not chose to take
		// one. just move on
		double velocityX = token.velocity.xVelocity;
		double velocityY = token.velocity.yVelocity;
		if (token.moveForward == false) {
			velocityX = -velocityX;
			velocityY = -velocityY;
		}
		token.xPosition = token.xPosition + velocityX;
		token.yPosition = token.yPosition + velocityY;

		token.velocity.distanceTakenInLane = token.velocity.distanceTakenInLane + token.velocity.distancePerStep;
	}


	private boolean isCrossWayBehindToken(Token token, CrossWay crossWay) {

		if (token.lasTakenCrossWays.contains(crossWay))
			return true;
		else
			return false;
	}


	private boolean handleTokenTakesCrossway(Token token, CrossWay crossWay) {
		// calculate the distance from token to crossway. we need this value
		// later on several times.
		double distanceToCrossway = token.velocity.distancePerStep
				- this.getDistance(token.xPosition, crossWay.xPosition, token.yPosition, crossWay.yPosition);

		// decide which lane to take. there might be more than one. if possible
		Map<StripePartConfiguration, Boolean> lanesReadyToTake = new HashMap<StripePartConfiguration, Boolean>();
		for (StripePartConfiguration possibleLane : crossWay.stripeParts) {
			if (possibleLane == token.myCurrentLane) {
				continue; // thats the lane we want to leave
			}

			boolean forwardPossible = false;
			double lengthFromBeginningToToken = this.getDistance(possibleLane.startXPositionInRoom, crossWay.xPosition,
					possibleLane.startYPositionInRoom, crossWay.yPosition) + distanceToCrossway;
			if (lengthFromBeginningToToken < getLengthOfLane(possibleLane)) {
				forwardPossible = true;
			}
			boolean backwardsPossible = false;
			double lengthFromEndToToken = this.getDistance(possibleLane.endXPositionInRoom, crossWay.xPosition,
					possibleLane.endYPositionInRoom, crossWay.yPosition) + distanceToCrossway;
			if (lengthFromEndToToken < getLengthOfLane(possibleLane)) {
				backwardsPossible = true;
			}

			// if we cannot use any direction continue with next cross
			if (forwardPossible == false && backwardsPossible == false) {
				continue;
			}

			// decide the prefered direction, forward or backward
			boolean forward = Math.random() > 0.5 ? true : false;

			if (forward && forwardPossible == false) {
				forward = false;
			}
			if (!forward && backwardsPossible == false) {
				forward = true;
			}

			lanesReadyToTake.put(possibleLane, forward);
		}

		if (lanesReadyToTake.size() == 0)
			return false;

		// now move the token to a new random lane
		int laneNumberChoosen = (int) (Math.random() * (lanesReadyToTake.size() - 1));
		StripePartConfiguration newLane = (StripePartConfiguration) lanesReadyToTake.keySet().toArray()[laneNumberChoosen];

		token.myCurrentLane = newLane;
		token.moveForward = lanesReadyToTake.get(newLane);
		token.xPosition = crossWay.xPosition;
		token.yPosition = crossWay.yPosition;

		if (this.speed >= 1) {
			token.velocity = token.getVelocityForToken(newLane, 1);
		} else {
			token.velocity = token.getVelocityForToken(newLane, this.speed);
		}
		double distanceLeftInPercent = distanceToCrossway / token.velocity.distancePerStep;
		double velocityX = token.velocity.xVelocity * distanceLeftInPercent;
		double velocityY = token.velocity.yVelocity * distanceLeftInPercent;

		if (token.moveForward == false) {
			velocityX = -velocityX;
			velocityY = -velocityY;
		}

		token.xPosition = token.xPosition + velocityX;
		token.yPosition = token.yPosition + velocityY;

		double startX;
		double startY;
		if (token.moveForward) {
			startX = token.myCurrentLane.startXPositionInRoom;
			startY = token.myCurrentLane.startYPositionInRoom;
		} else {
			startX = token.myCurrentLane.endXPositionInRoom;
			startY = token.myCurrentLane.endYPositionInRoom;
		}

		token.velocity.distanceTakenInLane = this.getDistance(startX, crossWay.xPosition, startY, crossWay.yPosition)
				+ distanceToCrossway;

		return true;
	}


	private double getLengthOfLane(StripePartConfiguration myCurrentLane) {
		return this.getDistance(myCurrentLane.startXPositionInRoom, myCurrentLane.endXPositionInRoom,
				myCurrentLane.startYPositionInRoom, myCurrentLane.endYPositionInRoom);
	}


	private Token createNewToken(double speed) {
		// get any lane
		StripePartConfiguration randomStripePart = this.lanes.get((int) ((Math.random() * this.lanes.size()))).stripePart;
		// get randomPosition on that StripePart
		Token result = new Token(randomStripePart, speed);

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
		if (result.moveForward) {
			result.velocity.distanceTakenInLane = getDistance(randomStripePart.startXPositionInRoom, result.xPosition,
					randomStripePart.startYPositionInRoom, result.yPosition);
		} else {
			result.velocity.distanceTakenInLane = getDistance(randomStripePart.endXPositionInRoom, result.xPosition,
					randomStripePart.endYPositionInRoom, result.yPosition);
		}
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
