package test;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.device.drivers.DummyLedStripeDeviceConfiguration;
import org.ambientlight.device.drivers.DummySwitchDeviceConfiguration;
import org.ambientlight.device.stripe.StripeConfiguration;
import org.ambientlight.device.stripe.StripePartConfiguration;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.StartNodeConfiguration;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.process.trigger.SceneryEntryEventTriggerConfiguration;
import org.ambientlight.process.trigger.SwitchEventTriggerConfiguration;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.actors.SwitchObjectConfiguration;
import org.ambientlight.room.entities.RoomConfigurationFactory;
import org.ambientlight.scenery.UserSceneryConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.actor.switching.SwitchingConfiguration;

public class CreateTestConfig {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DeviceDriverFactory df = new DeviceDriverFactory();
		CreateTestConfig test = new CreateTestConfig();

		RoomConfigurationFactory.saveRoomConfiguration(test.getTestRoom(),"backup");
	}

	public RoomConfiguration getTestRoom(){
		RoomConfiguration rc = new RoomConfiguration();
		
	
//		MultiStripeOverEthernetClientDeviceConfiguration dc = new MultiStripeOverEthernetClientDeviceConfiguration();
//		dc.hostName="192.168.1.44";
//		dc.port=2002;
//		
//		SwitchDeviceOverEthernetConfiguration switchingBridge = new SwitchDeviceOverEthernetConfiguration();
//		switchingBridge.hostName="localhost";
//		switchingBridge.port=2003;

		DummyLedStripeDeviceConfiguration dc = new DummyLedStripeDeviceConfiguration();
		DummySwitchDeviceConfiguration switchingBridge = new DummySwitchDeviceConfiguration();
		
		rc.deviceConfigurations.add(switchingBridge);
		rc.deviceConfigurations.add(dc);
		
		
		StripeConfiguration sc = new StripeConfiguration();
		sc.protocollType=StripeConfiguration.PROTOCOLL_TYPE_DIRECT_SPI;
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
	
		dc.configuredStripes.add(sc);

		
		SwitchObjectConfiguration sw1 = new SwitchObjectConfiguration();
		sw1.deviceType="ELRO";
		sw1.houseCode=15;
		sw1.switchingUnitCode=3;
		sw1.setName("kleine Stehlampe");		
		rc.actorConfigurations.put(sw1.getName(),sw1);

		LightObjectConfiguration lo = new LightObjectConfiguration();
		lo.setName("Schrank");
		lo.height=20;
		lo.layerNumber=2;
		lo.width=20;
		lo.xOffsetInRoom=5;
		lo.yOffsetInRoom=5;
		LightObjectConfiguration background = new LightObjectConfiguration();
		background.setName("background");
		background.height=200;
		background.layerNumber=1;
		background.width=200;
		background.xOffsetInRoom=2;
		background.yOffsetInRoom=2;
		rc.actorConfigurations.put(lo.getName(),lo);
		rc.actorConfigurations.put(background.getName(),background);
		
		SwitchEventTriggerConfiguration triggerMainSwitch = new SwitchEventTriggerConfiguration();
		triggerMainSwitch.name="RoomSwitch";
		triggerMainSwitch.setPowerState(false);
		rc.eventTriggerConfigurations.put(triggerMainSwitch.getName(),triggerMainSwitch);
		
		rc.height=400;
		rc.width=400;
		rc.roomName="testRoom";
		rc.currentScenery="scene1";

		UserSceneryConfiguration userScenario = new UserSceneryConfiguration();
		userScenario.id="scenario1";
		rc.sceneries.add(userScenario);
		
		ProcessConfiguration roomSwitchProcess = new ProcessConfiguration();
		roomSwitchProcess.id = "roomSwitch";
		userScenario.processIds.add(roomSwitchProcess.id);
		NodeConfiguration startNode = new NodeConfiguration();
		startNode.id=0;
		roomSwitchProcess.eventTriggerName=triggerMainSwitch.getName();
		
		PowerstateHandlerConfiguration powerstatehandler = new PowerstateHandlerConfiguration();
		powerstatehandler.nextNodeId=1;
		powerstatehandler.powerStateConfiguration.put(sw1.getName(), true);
		powerstatehandler.powerStateConfiguration.put(lo.getName(), true);
		startNode.actionHandler=powerstatehandler;
		
		NodeConfiguration termination = new NodeConfiguration();
		termination.id=1;
		termination.actionHandler=null;
		
		roomSwitchProcess.nodes.put(0,startNode);
		roomSwitchProcess.nodes.put(1,termination);
		rc.processes.add(roomSwitchProcess);
		
		ArrayList<LightObjectConfiguration> changeConfigFor = new ArrayList<LightObjectConfiguration>();
		changeConfigFor.add(background);
		changeConfigFor.add(lo);
		ArrayList<ActorConfiguration> turnLightOnFor = new ArrayList<ActorConfiguration>();
		turnLightOnFor.add(background);
		turnLightOnFor.add(sw1);
		
		createUserScenario(rc, userScenario, changeConfigFor,turnLightOnFor);
		
		return rc;
	}
	
	private void createUserScenario(RoomConfiguration rc, UserSceneryConfiguration userScenario, List<LightObjectConfiguration> lo, List<ActorConfiguration>  itemsToPutOn){
		
		ProcessConfiguration process = new ProcessConfiguration();
		process.id = "process-scenario1";
		NodeConfiguration startNode = new NodeConfiguration();
		startNode.id=0;
		
		SceneryEntryEventTriggerConfiguration triggerSceneryChange = new SceneryEntryEventTriggerConfiguration();
		triggerSceneryChange.name = "triggerScenarioEntry-"+userScenario.id;
		triggerSceneryChange.sceneryId=userScenario.id;
		rc.eventTriggerConfigurations.put(triggerSceneryChange.name, triggerSceneryChange);
		process.eventTriggerName=triggerSceneryChange.name;
		
		ConfigurationChangeHandlerConfiguration cHandler = new ConfigurationChangeHandlerConfiguration();
		cHandler.nextNodeId=1;
		for(LightObjectConfiguration current : lo){
			cHandler.addActorConfiguration(current.getName(), createSimpleColor());
		}
		startNode.actionHandler=cHandler;
		
		NodeConfiguration switchNode = new NodeConfiguration();
		switchNode.id=1;
		
		PowerstateHandlerConfiguration powerstatehandler = new PowerstateHandlerConfiguration();
		powerstatehandler.nextNodeId=2;
		for( ActorConfiguration current : itemsToPutOn){
			powerstatehandler.powerStateConfiguration.put(current.getName(), true);
		}
		switchNode.actionHandler=powerstatehandler;
		
		NodeConfiguration termination = new NodeConfiguration();
		termination.id=2;
		termination.actionHandler=null;
		process.nodes.put(0, startNode);
		process.nodes.put(1, switchNode);
		process.nodes.put(2, termination);
		
		rc.processes.add(process);
		userScenario.processIds.add(process.id);
	}
	
	
	
	private SimpleColorRenderingProgramConfiguration createSimpleColor(){
		
		SimpleColorRenderingProgramConfiguration scL01 = new SimpleColorRenderingProgramConfiguration();
		int i = (int) (Math.random()*256);
		
		Color color = new Color(i, 10, 100);
		scL01.rgb=color.getRGB();			
		return scL01;
	}
	
	
	private SwitchingConfiguration createSceneryMappingForSwitch(){
		SwitchingConfiguration config = new SwitchingConfiguration();
		return config;
	}
}
