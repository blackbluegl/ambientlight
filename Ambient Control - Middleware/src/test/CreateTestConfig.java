package test;

import java.io.IOException;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.device.drivers.DummyLedStripeDeviceConfiguration;
import org.ambientlight.device.drivers.DummySwitchDeviceConfiguration;
import org.ambientlight.device.stripe.StripeConfiguration;
import org.ambientlight.device.stripe.StripePartConfiguration;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.LightObjectConfiguration;
import org.ambientlight.room.objects.SwitchObjectConfiguration;
import org.ambientlight.scenery.entities.RoomFactory;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.switching.configuration.SwitchingConfiguration;

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
		
		SwitchObjectConfiguration sw1 = new SwitchObjectConfiguration();
		sw1.deviceType="ELRO";
		sw1.houseCode=31;
		sw1.switchingUnitCode=3;
		sw1.name="kleine Stehlampe";
		this.createSceneryMappingForSwitch(rc, sw1);
		sw1.getSceneryConfigurationBySceneryName("scene1").bypassOnSceneryChange=true;
		
		SwitchObjectConfiguration sw2 = new SwitchObjectConfiguration();
		sw2.deviceType="ELRO";
		sw2.houseCode=31;
		sw2.switchingUnitCode=2;
		sw2.name="große Stehlampe";
		this.createSceneryMappingForSwitch(rc, sw2);
		
		SwitchObjectConfiguration sw3 = new SwitchObjectConfiguration();
		sw3.deviceType="ELRO";
		sw3.houseCode=31;
		sw3.switchingUnitCode=1;
		sw3.name="blaue Lampe";
		this.createSceneryMappingForSwitch(rc, sw3);
		
		SwitchObjectConfiguration sw4 = new SwitchObjectConfiguration();
		sw4.deviceType="ELRO";
		sw4.houseCode=0;
		sw4.switchingUnitCode=3;
		sw4.name="Kati's Lampe";
		this.createSceneryMappingForSwitch(rc, sw4);
		
		SwitchObjectConfiguration sw5 = new SwitchObjectConfiguration();
		sw5.deviceType="ELRO";
		sw5.houseCode=0;
		sw5.switchingUnitCode=4;
		sw5.name="Flo's Lampe";
		this.createSceneryMappingForSwitch(rc, sw5);
		
		
		rc.roomItemConfigurations.add(sw1);
		rc.roomItemConfigurations.add(sw2);
		rc.roomItemConfigurations.add(sw3);
		rc.roomItemConfigurations.add(sw4);
		rc.roomItemConfigurations.add(sw5);
		
//		MultiStripeOverEthernetClientDeviceConfiguration dc = new MultiStripeOverEthernetClientDeviceConfiguration();
//		dc.hostName="192.168.1.36";
//		dc.port=2002;
		
//		SwitchDeviceOverEthernetConfiguration switchingBridge = new SwitchDeviceOverEthernetConfiguration();
//		switchingBridge.hostName="192.168.1.36";
//		switchingBridge.port=2003;

		DummyLedStripeDeviceConfiguration dc = new DummyLedStripeDeviceConfiguration();
		DummySwitchDeviceConfiguration switchingBridge = new DummySwitchDeviceConfiguration();
		
		rc.deviceConfigurations.add(switchingBridge);
		rc.deviceConfigurations.add(dc);
		
		
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
		
		LightObjectConfiguration background = new LightObjectConfiguration();
		background.name="background";
		LightObjectConfiguration lo = new LightObjectConfiguration();
		lo.name="Schrank";
		LightObjectConfiguration lo2 = new LightObjectConfiguration();
		lo2.name="Vitrine";
		LightObjectConfiguration lo3 = new LightObjectConfiguration();
		lo3.name="Vitrine 2";
		LightObjectConfiguration lo4 = new LightObjectConfiguration();
		lo4.name="Highboard";
		
//		SimpleColorRenderingProgramConfiguration currentColor = new SimpleColorRenderingProgramConfiguration();
//		currentColor.setB(0);
//		currentColor.setG(255);
//		currentColor.setR(0);
//		currentColor.powerState=true;
//		currentColor.sceneryName="scene1";
//		lo.currentSceneryConfiguration=currentColor;
		
		this.createSceneryMapping(rc, lo);
		lo.height=20;
		lo.layerNumber=2;
		lo.width=20;
		lo.xOffsetInRoom=5;
		lo.yOffsetInRoom=5;
		
//		
//		SimpleColorRenderingProgramConfiguration currentColor2 = new SimpleColorRenderingProgramConfiguration();
//		currentColor2.setB(0);
//		currentColor2.setG(0);
//		currentColor2.setR(255);
//		
//		currentColor2.powerState=true;
//		currentColor2.sceneryName="scene1";	
//		lo2.currentSceneryConfiguration=currentColor2;
		
		this.createSceneryMapping(rc, lo2);
		lo2.height=20;
		lo2.layerNumber=2;
		lo2.width=20;
		lo2.xOffsetInRoom=25;
		lo2.yOffsetInRoom=5;
		
		
//		SimpleColorRenderingProgramConfiguration currentColor3 = new SimpleColorRenderingProgramConfiguration();
//		currentColor3.setB(255);
//		currentColor3.setG(0);
//		currentColor3.setR(0);
//		currentColor3.powerState=true;
//		currentColor3.sceneryName="scene1";
//		lo3.currentSceneryConfiguration=currentColor3;
		
		this.createSceneryMapping(rc, lo3);
		lo3.height=20;
		lo3.layerNumber=2;
		lo3.width=20;
		lo3.xOffsetInRoom=45;
		lo3.yOffsetInRoom=5;
		
		
//		SimpleColorRenderingProgramConfiguration currentColor4 = new SimpleColorRenderingProgramConfiguration();
//		currentColor4.setB(255);
//		currentColor4.setG(0);
//		currentColor4.setR(255);
//		currentColor4.powerState=true;
//		currentColor4.sceneryName="scene1";
//		lo4.currentSceneryConfiguration=currentColor4;
		
		this.createSceneryMapping(rc, lo4);
		lo4.height=20;
		lo4.layerNumber=3;
		lo4.width=20;
		lo4.xOffsetInRoom=65;
		lo4.yOffsetInRoom=5;
		
		
//		SimpleColorRenderingProgramConfiguration currentColorBG = new SimpleColorRenderingProgramConfiguration();
//		currentColorBG.setB(20);
//		currentColorBG.setG(20);
//		currentColorBG.setR(20);
//		currentColorBG.powerState=true;
//		currentColorBG.sceneryName="scene1";
//		background.currentSceneryConfiguration=currentColorBG;
		
		this.createSceneryMapping(rc, background);
		background.height=200;
		background.layerNumber=1;
		background.width=200;
		background.xOffsetInRoom=2;
		background.yOffsetInRoom=2;
		
		
		rc.roomItemConfigurations.add(lo);
		rc.roomItemConfigurations.add(lo2);
		rc.roomItemConfigurations.add(lo3);
		rc.roomItemConfigurations.add(lo4);
		rc.roomItemConfigurations.add(background);
		
		
		rc.height=400;
		rc.width=400;
		rc.roomName="testRoom";
		rc.currentScenery="scene1";

		return rc;
	}
	
	private void createSceneryMapping(RoomConfiguration rc, LightObjectConfiguration lc){
		
		SimpleColorRenderingProgramConfiguration scL01 = new SimpleColorRenderingProgramConfiguration();
		int i = (int) (Math.random()*256);
		scL01.setB(i);
		scL01.setG(10);
		scL01.setR(100);
		scL01.powerState=true;
		
		SimpleColorRenderingProgramConfiguration scL02 = new SimpleColorRenderingProgramConfiguration();
		scL02.setB(122);
		scL02.setG(i);
		scL02.setR(100);
		scL02.powerState=true;
		lc.sceneryConfigurationBySzeneryName.put("scene1", scL01);
		lc.sceneryConfigurationBySzeneryName.put("scene2", scL02);
	}
	
	
	private void createSceneryMappingForSwitch(RoomConfiguration rc, SwitchObjectConfiguration sc){
		SwitchingConfiguration config = new SwitchingConfiguration();
		config.powerState=true;
		
		SwitchingConfiguration config2 = new SwitchingConfiguration();
		config2.powerState=false;

		sc.sceneryConfigurationBySzeneryName.put("scene1", config);
		sc.sceneryConfigurationBySzeneryName.put("scene2", config2);
		
	}
		
		
	
}
