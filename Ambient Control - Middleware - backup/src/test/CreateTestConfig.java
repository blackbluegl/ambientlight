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
import org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.process.handler.event.EventToBooleanHandlerConfiguration;
import org.ambientlight.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.process.handler.expression.ExpressionConfiguration;
import org.ambientlight.process.trigger.SceneryEntryEventTriggerConfiguration;
import org.ambientlight.process.trigger.SwitchEventTriggerConfiguration;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.actors.SwitchObjectConfiguration;
import org.ambientlight.room.entities.RoomConfigurationFactory;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;
import org.ambientlight.scenery.UserSceneryConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration;


public class CreateTestConfig {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		DeviceDriverFactory df = new DeviceDriverFactory();
		CreateTestConfig test = new CreateTestConfig();

		RoomConfigurationFactory.saveRoomConfiguration(test.getTestRoom(), "backup");
	}


	public RoomConfiguration getTestRoom() {
		RoomConfiguration rc = new RoomConfiguration();

		// MultiStripeOverEthernetClientDeviceConfiguration dc = new
		// MultiStripeOverEthernetClientDeviceConfiguration();
		// dc.hostName="192.168.1.44";
		// dc.port=2002;
		//
		// SwitchDeviceOverEthernetConfiguration switchingBridge = new
		// SwitchDeviceOverEthernetConfiguration();
		// switchingBridge.hostName="localhost";
		// switchingBridge.port=2003;

		DummyLedStripeDeviceConfiguration dc = new DummyLedStripeDeviceConfiguration();
		DummySwitchDeviceConfiguration switchingBridge = new DummySwitchDeviceConfiguration();

		rc.deviceConfigurations.add(switchingBridge);
		rc.deviceConfigurations.add(dc);

		StripeConfiguration sc = new StripeConfiguration();
		sc.protocollType = StripeConfiguration.PROTOCOLL_TYPE_DIRECT_SPI;
		sc.pixelAmount = 128;
		sc.port = 0;

		StripePartConfiguration spLo1S1 = new StripePartConfiguration();
		spLo1S1.endXPositionInRoom = 24;
		spLo1S1.endYPositionInRoom = 5;
		spLo1S1.offsetInStripe = 0;
		spLo1S1.pixelAmount = 20;
		spLo1S1.startXPositionInRoom = 5;
		spLo1S1.startYPositionInRoom = 5;
		sc.stripeParts.add(spLo1S1);

		dc.configuredStripes.add(sc);

		SwitchObjectConfiguration sw1 = new SwitchObjectConfiguration();
		sw1.deviceType = "ELRO";
		sw1.houseCode = 15;
		sw1.switchingUnitCode = 3;
		sw1.setName("kleine Stehlampe");
		rc.actorConfigurations.put(sw1.getName(), sw1);

		LightObjectConfiguration lo = new LightObjectConfiguration();
		lo.setName("Schrank");
		lo.height = 20;
		lo.layerNumber = 2;
		lo.width = 20;
		lo.xOffsetInRoom = 5;
		lo.yOffsetInRoom = 5;
		lo.renderingProgramConfiguration = this.createSimpleColor();
		rc.actorConfigurations.put(lo.getName(), lo);

		LightObjectConfiguration background = new LightObjectConfiguration();
		background.setName("background");
		background.height = 200;
		background.layerNumber = 1;
		background.width = 200;
		background.xOffsetInRoom = 2;
		background.yOffsetInRoom = 2;
		background.renderingProgramConfiguration = this.createSimpleColor();
		rc.actorConfigurations.put(background.getName(), background);

		SwitchEventGeneratorConfiguration triggerMainSwitch = new SwitchEventGeneratorConfiguration();
		triggerMainSwitch.name = "RoomSwitch";
		rc.eventGeneratorConfigurations.add(triggerMainSwitch);

		rc.height = 400;
		rc.width = 400;
		rc.roomName = "testRoom";

		UserSceneryConfiguration userScenario = new UserSceneryConfiguration();
		userScenario.id = "scenario1";
		rc.sceneries.add(userScenario);
		rc.currentSceneryConfig = userScenario;

		ProcessConfiguration roomSwitchProcess = new ProcessConfiguration();
		roomSwitchProcess.id = "roomSwitch";
		rc.processes.add(roomSwitchProcess);
		SwitchEventTriggerConfiguration triggerConf = new SwitchEventTriggerConfiguration();
		triggerConf.eventGeneratorName = triggerMainSwitch.name;
		roomSwitchProcess.eventTriggerConfiguration = triggerConf;

		NodeConfiguration eventMapperNode = new NodeConfiguration();
		eventMapperNode.id = 0;
		EventToBooleanHandlerConfiguration eventMapper = new EventToBooleanHandlerConfiguration();
		eventMapper.nextNodeId = 1;
		eventMapperNode.actionHandler = eventMapper;
		roomSwitchProcess.nodes.put(0, eventMapperNode);

		NodeConfiguration decissionNode = new NodeConfiguration();
		decissionNode.id = 1;
		DecisionHandlerConfiguration decission = new DecisionHandlerConfiguration();
		decission.nextNodeId = 2;
		decission.nextAlternativeNodeId = 3;
		ExpressionConfiguration expression = new ExpressionConfiguration();
		decission.expressionConfiguration = expression;
		expression.expression = "#{tokenValue}==1.0";
		decissionNode.actionHandler = decission;
		roomSwitchProcess.nodes.put(1, decissionNode);

		NodeConfiguration turnOnNode = new NodeConfiguration();
		turnOnNode.id = 2;
		PowerstateHandlerConfiguration powerOnHandler = new PowerstateHandlerConfiguration();
		powerOnHandler.nextNodeId = null;
		powerOnHandler.powerStateConfiguration.put(sw1.getName(), true);
		powerOnHandler.powerStateConfiguration.put(lo.getName(), true);
		turnOnNode.actionHandler = powerOnHandler;
		roomSwitchProcess.nodes.put(2, turnOnNode);

		NodeConfiguration turnOffNode = new NodeConfiguration();
		turnOffNode.id = 3;
		SimplePowerStateHandlerConfiguration powerDownHandler = new SimplePowerStateHandlerConfiguration();
		powerDownHandler.powerState = false;
		powerDownHandler.nextNodeId = null;
		turnOffNode.actionHandler = powerDownHandler;
		roomSwitchProcess.nodes.put(3, turnOffNode);

		ArrayList<LightObjectConfiguration> changeConfigFor = new ArrayList<LightObjectConfiguration>();
		changeConfigFor.add(background);
		changeConfigFor.add(lo);
		ArrayList<ActorConfiguration> turnLightOnFor = new ArrayList<ActorConfiguration>();
		turnLightOnFor.add(background);
		turnLightOnFor.add(sw1);

		createUserScenario(rc, userScenario, changeConfigFor, turnLightOnFor);

		return rc;
	}


	private void createUserScenario(RoomConfiguration rc, UserSceneryConfiguration userScenario,
			List<LightObjectConfiguration> lo, List<ActorConfiguration> itemsToPutOn) {

		ProcessConfiguration process = new ProcessConfiguration();
		process.id = "process-scenario1";
		NodeConfiguration startNode = new NodeConfiguration();
		startNode.id = 0;

		SceneryEntryEventTriggerConfiguration triggerSceneryChange = new SceneryEntryEventTriggerConfiguration();
		triggerSceneryChange.sceneryName = "triggerScenarioEntry-" + userScenario.id;
		process.eventTriggerConfiguration = triggerSceneryChange;

		ConfigurationChangeHandlerConfiguration cHandler = new ConfigurationChangeHandlerConfiguration();
		cHandler.nextNodeId = 1;
		for (LightObjectConfiguration current : lo) {
			cHandler.addActorConfiguration(current.getName(), createSimpleColor());
		}
		startNode.actionHandler = cHandler;

		NodeConfiguration switchNode = new NodeConfiguration();
		switchNode.id = 1;

		PowerstateHandlerConfiguration powerstatehandler = new PowerstateHandlerConfiguration();
		powerstatehandler.nextNodeId = null;
		for (ActorConfiguration current : itemsToPutOn) {
			powerstatehandler.powerStateConfiguration.put(current.getName(), true);
		}
		switchNode.actionHandler = powerstatehandler;

		process.nodes.put(0, startNode);
		process.nodes.put(1, switchNode);

		rc.processes.add(process);
	}


	private SimpleColorRenderingProgramConfiguration createSimpleColor() {

		SimpleColorRenderingProgramConfiguration scL01 = new SimpleColorRenderingProgramConfiguration();
		int i = (int) (Math.random() * 256);

		Color color = new Color(i, 10, 100);
		scL01.rgb = color.getRGB();
		return scL01;
	}
}
