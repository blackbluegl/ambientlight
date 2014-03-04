package org.ambientlight.room.entities.lightobject;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

import org.ambientlight.debug.BufferedImageDisplayOutput;
import org.ambientlight.device.drivers.AnimateableLedDevice;
import org.ambientlight.device.led.StripePart;


public class RenderingTask extends TimerTask {

	private Renderer renderer;
	private LightObjectManager manager;
	private List<AnimateableLedDevice> devices;
	private boolean debug;

	public BufferedImageDisplayOutput debugRoomDisplay;

	public RenderingTask(Renderer renderer, LightObjectManager manager, List<AnimateableLedDevice> devices, boolean debug) {
		super();
		this.renderer = renderer;
		this.manager = manager;
		this.devices = devices;
		this.debug = debug;
	}


	private void resetConnection() {
		for (AnimateableLedDevice currentDevice : devices) {
			try {
				System.out.println("resetting connection");
				currentDevice.closeConnection();
				currentDevice.connect();
				System.out.println("resetting connection ok");
			} catch (Exception e) {
				System.out.println("resetting connection not ok:"+e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {

		if (debug) {
			handleDebug();
		}

		for (AnimateableLedDevice currentDevice : devices) {
			try {
				if (renderer.hadDirtyRegionInLastRun()) {
					currentDevice.writeData();
				}
			} catch (IOException e) {
				e.printStackTrace();
				this.resetConnection();
			}
		}
	}

	private void handleDebug() {
		if(this.debugRoomDisplay == null){
			this.debugRoomDisplay = new BufferedImageDisplayOutput(manager.getPixelMap().getWidth(), manager.getPixelMap()
					.getHeight(),
					"RoomContent");
		}

		Graphics2D g2d = manager.getPixelMap().createGraphics();
		AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.6f);
		g2d.setComposite(alpha);
		Font font = new Font("Arial", Font.BOLD, 8);
		g2d.setFont(font);

		for (StripePart current : manager.getAllStripePartsInRoom()) {
			g2d.setColor(Color.BLACK);
			g2d.drawLine(current.configuration.startXPositionInRoom, current.configuration.startYPositionInRoom, 
					current.configuration.endXPositionInRoom, current.configuration.endYPositionInRoom);
			g2d.setColor(Color.GREEN);
			g2d.drawLine(current.configuration.startXPositionInRoom, current.configuration.startYPositionInRoom,current.configuration.startXPositionInRoom, current.configuration.startYPositionInRoom);
			g2d.drawLine(current.configuration.endXPositionInRoom, current.configuration.endYPositionInRoom,current.configuration.endXPositionInRoom, current.configuration.endYPositionInRoom);
			String info = String.valueOf(current.configuration.offsetInStripe)+" "+String.valueOf(current.configuration.pixelAmount);
			g2d.setColor(Color.WHITE);
			int stringXPos= (current.configuration.startXPositionInRoom -current.configuration.endXPositionInRoom)/2;
			if(stringXPos>0) {
				stringXPos=-stringXPos;
			} else{
				stringXPos=Math.abs(stringXPos);
			}
			int stringYPos= (current.configuration.startYPositionInRoom -current.configuration.endYPositionInRoom)/2;
			if(stringYPos>0) {
				stringYPos=-stringYPos;
			} else{
				stringYPos=Math.abs(stringYPos);
			}
			stringXPos=current.configuration.startXPositionInRoom+stringXPos;
			stringYPos=current.configuration.startYPositionInRoom+stringYPos;
			g2d.drawString(info, stringXPos,stringYPos);
		}
		debugRoomDisplay.setImageContent(manager.getPixelMap());
	}
}
