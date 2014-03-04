package test;



public class CreateTronTestConfig {
	//
	// /**
	// * @param args
	// * @throws IOException
	// */
	// public static void main(String[] args) throws IOException {
	// DeviceDriverFactory df = new DeviceDriverFactory();
	// CreateTronTestConfig test = new CreateTronTestConfig();
	//
	// RoomConfigurationFactory.saveRoomConfiguration(test.getTestRoom(),
	// "backup");
	// }
	//
	//
	// public RoomConfiguration getTestRoom() {
	// RoomConfiguration rc = new RoomConfiguration();
	//
	// MultiStripeOverEthernetClientDeviceConfiguration dc = new
	// MultiStripeOverEthernetClientDeviceConfiguration();
	// dc.port=2002;
	// dc.hostName="192.168.1.44";
	//
	// // DummyLedStripeDeviceConfiguration dc = new
	// DummyLedStripeDeviceConfiguration();
	// rc.deviceConfigurations.add(dc);
	//
	// StripeConfiguration sc = new StripeConfiguration();
	// sc.protocollType=sc.PROTOCOLL_TYPE_TM1812;
	// sc.pixelAmount = 128;
	// sc.port = 0;
	//
	// StripePartConfiguration spLo1S1 = new StripePartConfiguration();
	// spLo1S1.endXPositionInRoom = 240;
	// spLo1S1.endYPositionInRoom = 5;
	// spLo1S1.offsetInStripe = 0;
	// spLo1S1.pixelAmount = 128;
	// spLo1S1.startXPositionInRoom = 5;
	// spLo1S1.startYPositionInRoom = 5;
	// sc.stripeParts.add(spLo1S1);
	//
	// StripePartConfiguration spLo1S2 = new StripePartConfiguration();
	// spLo1S2.startXPositionInRoom = 240;
	// spLo1S2.startYPositionInRoom = 5;
	// spLo1S2.endXPositionInRoom = 240;
	// spLo1S2.endYPositionInRoom = 240;
	// spLo1S2.offsetInStripe = 20;
	// spLo1S2.pixelAmount = 20;
	// sc.stripeParts.add(spLo1S2);
	//
	// StripePartConfiguration spLo1S3 = new StripePartConfiguration();
	// spLo1S3.startXPositionInRoom = 240;
	// spLo1S3.startYPositionInRoom = 240;
	// spLo1S3.endXPositionInRoom = 5;
	// spLo1S3.endYPositionInRoom = 240;
	// spLo1S3.offsetInStripe = 40;
	// spLo1S3.pixelAmount = 20;
	// sc.stripeParts.add(spLo1S3);
	//
	// StripePartConfiguration spLo1S4 = new StripePartConfiguration();
	// spLo1S4.startXPositionInRoom = 5;
	// spLo1S4.startYPositionInRoom = 240;
	// spLo1S4.endXPositionInRoom = 5;
	// spLo1S4.endYPositionInRoom = 5;
	// spLo1S4.offsetInStripe = 60;
	// spLo1S4.pixelAmount = 20;
	// sc.stripeParts.add(spLo1S4);
	//
	// StripePartConfiguration spLo1S5 = new StripePartConfiguration();
	// spLo1S5.startXPositionInRoom = 6;
	// spLo1S5.startYPositionInRoom = 120;
	// spLo1S5.endXPositionInRoom = 240;
	// spLo1S5.endYPositionInRoom = 120;
	// spLo1S5.offsetInStripe = 80;
	// spLo1S5.pixelAmount = 20;
	// sc.stripeParts.add(spLo1S5);
	//
	// dc.configuredStripes.add(sc);
	//
	// LightObjectConfiguration background = new LightObjectConfiguration();
	// background.name = "background";
	// LightObjectConfiguration lo = new LightObjectConfiguration();
	// lo.name = "Schrank";
	//
	// this.createSceneryMapping(rc, lo);
	// lo.height = 250;
	// lo.layerNumber = 2;
	// lo.width = 250;
	// lo.xOffsetInRoom = 5;
	// lo.yOffsetInRoom = 5;
	//
	// SimpleColorRenderingProgramConfiguration simpleColor = new
	// SimpleColorRenderingProgramConfiguration();
	// // simpleColor.powerState = true;
	// Color c = new Color(20,20,20);
	// simpleColor.rgb=c.getRGB();
	// // background.sceneryConfigurationBySzeneryName.put("scene1",
	// simpleColor);
	// background.height = 200;
	// background.layerNumber = 1;
	// background.width = 200;
	// background.xOffsetInRoom = 2;
	// background.yOffsetInRoom = 2;
	//
	// rc.actorConfigurations.add(lo);
	// rc.actorConfigurations.add(background);
	//
	// rc.height = 400;
	// rc.width = 400;
	// rc.roomName = "testRoom";
	// rc.currentScenery = "scene1";
	//
	// return rc;
	// }
	//
	//
	// private void createSceneryMapping(RoomConfiguration rc,
	// LightObjectConfiguration lc) {
	//
	// TronRenderingProgrammConfiguration config = new
	// TronRenderingProgrammConfiguration();
	// config.lightPointAmount=(1);
	// config.speed=(1);
	// // config.powerState = true;
	// Color c = new Color(20,20,20);
	// config.rgb=c.getRGB();
	// config.lightImpact=(0.8);
	// config.sparkleSize=(0.1);
	// config.sparkleStrength=(0.3);
	// config.tailLength=(0.2);
	// // lc.sceneryConfigurationBySzeneryName.put("scene1", config);
	// }

}
