package org.ambientlight.config.device.led;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("stripePart")
public class StripePartConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public int startXPositionInRoom;
	public int startYPositionInRoom;
	public int endXPositionInRoom;
	public int endYPositionInRoom;
	public int offsetInStripe;
	public int pixelAmount;

}
