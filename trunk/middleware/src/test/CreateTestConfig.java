package test;

import java.io.IOException;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.device.drivers.configuration.DummyDeviceConfiguration;
import org.ambientlight.device.drivers.dummy.DummyDeviceDriver;
import org.ambientlight.device.stripe.configuration.StripeConfiguration;
import org.ambientlight.scenery.entities.RoomFactory;
import org.ambientlight.scenery.entities.configuration.LightObjectConfiguration;
import org.ambientlight.scenery.entities.configuration.RoomConfiguration;
import org.ambientlight.scenery.entities.configuration.StripePartConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;

public class CreateTestConfig {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DeviceDriverFactory df = new DeviceDriverFactory();
		RoomFactory rf = new RoomFactory(df);
		CreateTestConfig test = new CreateTestConfig();

		rf.saveRoomConfiguration(test.getTestRoom(),"backup");
	}

	public RoomConfiguration getTestRoom(){
		RoomConfiguration rc = new RoomConfiguration();
		
	
		
		DummyDeviceConfiguration dc = new DummyDeviceConfiguration();
		dc.driverName=DummyDeviceDriver.class.getSimpleName();
//		MultiStripeOverEthernetClientDeviceConfiguration dc = new MultiStripeOverEthernetClientDeviceConfiguration();
//		dc.hostName="192.168.1.34";
//		dc.port=2002;
//		dc.driverName=MultistripeOverEthernetClientDeviceDriver.class.getSimpleName();
		
		
		StripeConfiguration sc = new StripeConfiguration();
		sc.pixelAmount=128;
		sc.port=0;
	
		StripePartConfiguration spLo1S1 = new StripePartConfiguration();
		spLo1S1.endXPositionInRoom=24;
		spLo1S1.endYPositionInRoom=5;
		spLo1S1.offsetInStripe=0;
		spLo1S1.pixelAmount=20;
		spLo1S1.startXPositionInRoom=5;
		spLo1S1.startYPositionInRoom=5;
		sc.stripeParts.add(spLo1S1);
		
		StripePartConfiguration spLo1S2 = new StripePartConfiguration();
		spLo1S2.startXPositionInRoom=24;
		spLo1S2.startYPositionInRoom=5;
		spLo1S2.endXPositionInRoom=24;
		spLo1S2.endYPositionInRoom=24;
		spLo1S2.offsetInStripe=20;
		spLo1S2.pixelAmount=20;
		sc.stripeParts.add(spLo1S2);
		
		StripePartConfiguration spLo1S3 = new StripePartConfiguration();
		spLo1S3.startXPositionInRoom=24;
		spLo1S3.startYPositionInRoom=24;
		spLo1S3.endXPositionInRoom=5;
		spLo1S3.endYPositionInRoom=24;
		spLo1S3.offsetInStripe=40;
		spLo1S3.pixelAmount=20;		
		sc.stripeParts.add(spLo1S3);

		StripePartConfiguration spLo1S4 = new StripePartConfiguration();
		spLo1S4.startXPositionInRoom=5;
		spLo1S4.startYPositionInRoom=24;
		spLo1S4.endXPositionInRoom=5;
		spLo1S4.endYPositionInRoom=5;
		spLo1S4.offsetInStripe=60;
		spLo1S4.pixelAmount=20;
		sc.stripeParts.add(spLo1S4);
		
		StripePartConfiguration spLo1S5 = new StripePartConfiguration();
		spLo1S5.startXPositionInRoom=5;
		spLo1S5.startYPositionInRoom=15;
		spLo1S5.endXPositionInRoom=24;
		spLo1S5.endYPositionInRoom=15;
		spLo1S5.offsetInStripe=80;
		spLo1S5.pixelAmount=20;
		sc.stripeParts.add(spLo1S5);
		
		StripePartConfiguration spLo2 = new StripePartConfiguration();
		spLo2.endXPositionInRoom=35;
		spLo2.endYPositionInRoom=20;
		spLo2.offsetInStripe=100;
		spLo2.pixelAmount=10;
		spLo2.startXPositionInRoom=25;
		spLo2.startYPositionInRoom=10;
		sc.stripeParts.add(spLo2);
		
		dc.configuredStripes.add(sc);
	
		rc.devices.add(dc);
		
		LightObjectConfiguration background = new LightObjectConfiguration();
		background.lightObjectName="background";
		LightObjectConfiguration lo = new LightObjectConfiguration();
		lo.lightObjectName="Schrank";
		LightObjectConfiguration lo2 = new LightObjectConfiguration();
		lo2.lightObjectName="Vitrine";
		LightObjectConfiguration lo3 = new LightObjectConfiguration();
		lo3.lightObjectName="Vitrine 2";
		LightObjectConfiguration lo4 = new LightObjectConfiguration();
		lo4.lightObjectName="Highboard";
		
		SimpleColorRenderingProgramConfiguration currentColor = new SimpleColorRenderingProgramConfiguration();
		currentColor.setB(0);
		currentColor.setG(255);
		currentColor.setR(0);
		currentColor.powerState=true;
		currentColor.sceneryName="scene1";
		
		lo.currentRenderingProgrammConfiguration=currentColor;
		this.createSceneryMapping(rc, lo);
		lo.height=20;
		lo.layerNumber=2;
		lo.width=20;
		lo.xOffsetInRoom=5;
		lo.yOffsetInRoom=5;
		
		
		SimpleColorRenderingProgramConfiguration currentColor2 = new SimpleColorRenderingProgramConfiguration();
		currentColor2.setB(0);
		currentColor2.setG(0);
		currentColor2.setR(255);
		
		currentColor2.powerState=true;
		currentColor2.sceneryName="scene1";
		
		lo2.currentRenderingProgrammConfiguration=currentColor2;
		this.createSceneryMapping(rc, lo2);
		lo2.height=20;
		lo2.layerNumber=2;
		lo2.width=20;
		lo2.xOffsetInRoom=25;
		lo2.yOffsetInRoom=5;
		
		
		SimpleColorRenderingProgramConfiguration currentColor3 = new SimpleColorRenderingProgramConfiguration();
		currentColor3.setB(255);
		currentColor3.setG(0);
		currentColor3.setR(0);
		currentColor3.powerState=true;
		currentColor3.sceneryName="scene1";
		lo3.currentRenderingProgrammConfiguration=currentColor3;
		this.createSceneryMapping(rc, lo3);
		lo3.height=20;
		lo3.layerNumber=2;
		lo3.width=20;
		lo3.xOffsetInRoom=45;
		lo3.yOffsetInRoom=5;
		
		
		SimpleColorRenderingProgramConfiguration currentColor4 = new SimpleColorRenderingProgramConfiguration();
		currentColor4.setB(255);
		currentColor4.setG(0);
		currentColor4.setR(255);
		currentColor4.powerState=true;
		currentColor4.sceneryName="scene1";
		
		lo4.currentRenderingProgrammConfiguration=currentColor4;
		this.createSceneryMapping(rc, lo4);
		lo4.height=20;
		lo4.layerNumber=3;
		lo4.width=20;
		lo4.xOffsetInRoom=65;
		lo4.yOffsetInRoom=5;
		
		
		SimpleColorRenderingProgramConfiguration currentColorBG = new SimpleColorRenderingProgramConfiguration();
		currentColorBG.setB(20);
		currentColorBG.setG(20);
		currentColorBG.setR(20);
		currentColorBG.powerState=true;
		currentColorBG.sceneryName="scene1";
		
		background.currentRenderingProgrammConfiguration=currentColorBG;
		this.createSceneryMapping(rc, background);
		background.height=200;
		background.layerNumber=1;
		background.width=200;
		background.xOffsetInRoom=2;
		background.yOffsetInRoom=2;
		
		
		rc.lightObjects.add(lo);
		rc.lightObjects.add(lo2);
		rc.lightObjects.add(lo3);
		rc.lightObjects.add(lo4);
		rc.lightObjects.add(background);
		
		
		rc.height=400;
		rc.width=400;
		rc.roomName="testRoom";

		return rc;
	}
	
	private void createSceneryMapping(RoomConfiguration rc, LightObjectConfiguration lc){
		
		SimpleColorRenderingProgramConfiguration scL01 = new SimpleColorRenderingProgramConfiguration();
		int i = (int) (Math.random()*256);
		scL01.setB(i);
		scL01.setG(10);
		scL01.setR(100);
		scL01.sceneryName="scene1";
		scL01.powerState=true;
		
		SimpleColorRenderingProgramConfiguration scL02 = new SimpleColorRenderingProgramConfiguration();
		scL02.setB(122);
		scL02.setG(i);
		scL02.setR(100);
		scL02.sceneryName="scene2";
		scL02.powerState=true;

		lc.renderingProgrammConfigurationBySzeneryName.add(scL01);
		lc.renderingProgrammConfigurationBySzeneryName.add(scL02);
	}
}
