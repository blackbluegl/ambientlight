package org.ambientlight.scenery.entities.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("stripePart")
public class StripePartConfiguration {

	public int startXPositionInRoom;
	public int startYPositionInRoom;
	public int endXPositionInRoom;
	public int endYPositionInRoom;
	public int offsetInStripe;
	public int pixelAmount;

}