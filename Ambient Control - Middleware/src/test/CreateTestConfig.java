package test;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.device.drivers.DummyLedStripeDeviceConfiguration;
import org.ambientlight.device.drivers.DummySwitchDeviceConfiguration;
import org.ambientlight.device.led.StripeConfiguration;
import org.ambientlight.device.led.StripePartConfiguration;
import org.ambientlight.process.NodeConfiguration;
import org.ambientlight.process.ProcessConfiguration;
import org.ambientlight.process.events.SceneryEntryEventConfiguration;
import org.ambientlight.process.events.SwitchEventConfiguration;
import org.ambientlight.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.process.handler.event.EventGeneratorSensorAdapterConfiguration;
import org.ambientlight.process.handler.event.EventToBooleanHandlerConfiguration;
import org.ambientlight.process.handler.event.FireEventHandlerConfiguration;
import org.ambientlight.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.process.handler.expression.ExpressionConfiguration;
import org.ambientlight.room.IUserRoomItem;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.actors.SwitchObjectConfiguration;
import org.ambientlight.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.room.eventgenerator.SwitchEventGeneratorConfiguration;
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

		RoomConfigurationFactory.saveRoomConfiguration(test.getTestRoom(), "default");
	}


	public RoomConfiguration getTestRoom() {
		RoomConfiguration rc = new RoomConfiguration();

		DummyLedStripeDeviceConfiguration dc = new DummyLedStripeDeviceConfiguration();
		DummySwitchDeviceConfiguration switchingBridge = new DummySwitchDeviceConfiguration();

		// MultiStripeOverEthernetClientDeviceConfiguration dc = new
		// MultiStripeOverEthernetClientDeviceConfiguration();
		// dc.hostName = "ambi-schlafen";
		// dc.port = 2002;
		//
		// SwitchDeviceOverEthernetConfiguration switchingBridge = new
		// SwitchDeviceOverEthernetConfiguration();
		// switchingBridge.hostName = "rfmbridge";
		// switchingBridge.port = 2003;

		// LK35CLientDeviceConfiguration lk35 = new
		// LK35CLientDeviceConfiguration();
		// lk35.hostName = "ambi-lk35-2";
		// lk35.port = 8899;
		// LedPointConfiguration ledPoint = new LedPointConfiguration();
		// ledPoint.xPosition = 10;
		// ledPoint.yPosition = 10;
		// ledPoint.port = 1;
		// lk35.configuredLeds.add(ledPoint);
		// rc.deviceConfigurations.add(lk35);
		//
		// LedPointConfiguration ledPoint2 = new LedPointConfiguration();
		// ledPoint2.xPosition = 40;
		// ledPoint2.yPosition = 30;
		// ledPoint2.port = 2;
		// lk35.configuredLeds.add(ledPoint2);



		rc.deviceConfigurations.add(switchingBridge);
		rc.deviceConfigurations.add(dc);

		StripeConfiguration sc = new StripeConfiguration();
		sc.protocollType = StripeConfiguration.PROTOCOLL_TYPE_TM1812;
		sc.pixelAmount = 162;
		sc.port = 0;

		StripePartConfiguration spLo1S1 = new StripePartConfiguration();
		spLo1S1.endXPositionInRoom = 161;
		spLo1S1.endYPositionInRoom = 0;
		spLo1S1.offsetInStripe = 0;
		spLo1S1.pixelAmount = 162;
		spLo1S1.startXPositionInRoom = 0;
		spLo1S1.startYPositionInRoom = 0;
		sc.stripeParts.add(spLo1S1);

		dc.configuredStripes.add(sc);

		SwitchObjectConfiguration sw1 = new SwitchObjectConfiguration();
		sw1.deviceType = "ELRO";
		sw1.houseCode = 15;
		sw1.switchingUnitCode = 3;
		sw1.setName("kleine Stehlampe");
		sw1.actorConductConfiguration = new SwitchingConfiguration();
		rc.actorConfigurations.put(sw1.getName(), sw1);

		LightObjectConfiguration lo = new LightObjectConfiguration();
		lo.setName("Schrank");
		lo.height = 20;
		lo.layerNumber = 2;
		lo.width = 20;
		lo.xOffsetInRoom = 5;
		lo.yOffsetInRoom = 5;
		lo.actorConductConfiguration = this.createSimpleColor();
		rc.actorConfigurations.put(lo.getName(), lo);

		LightObjectConfiguration background = new LightObjectConfiguration();
		background.setName("background");
		background.height = 200;
		background.layerNumber = 1;
		background.width = 200;
		background.xOffsetInRoom = 2;
		background.yOffsetInRoom = 2;
		background.actorConductConfiguration = this.createSimpleColor();
		rc.actorConfigurations.put(background.getName(), background);

		SwitchEventGeneratorConfiguration triggerMainSwitch = new SwitchEventGeneratorConfiguration();
		triggerMainSwitch.name = "RoomSwitch";
		rc.eventGeneratorConfigurations.add(triggerMainSwitch);

		SceneryEventGeneratorConfiguration sceneryEventGenerator = new SceneryEventGeneratorConfiguration();
		sceneryEventGenerator.name = "RoomSceneryEventGenerator";
		rc.eventGeneratorConfigurations.add(sceneryEventGenerator);

		rc.height = 400;
		rc.width = 400;
		rc.roomName = "testRoom";

		UserSceneryConfiguration userScenario = new UserSceneryConfiguration();
		userScenario.id = "scenario1";
		rc.sceneries.add(userScenario);

		UserSceneryConfiguration userScenario2 = new UserSceneryConfiguration();
		userScenario2.id = "scenario2";
		rc.sceneries.add(userScenario2);

		sceneryEventGenerator.currentScenery = userScenario;

		ProcessConfiguration roomSwitchProcess = new ProcessConfiguration();
		roomSwitchProcess.id = "roomSwitch";
		rc.processes.add(roomSwitchProcess);
		SwitchEventConfiguration triggerConfOn = new SwitchEventConfiguration();
		triggerConfOn.eventGeneratorName = triggerMainSwitch.name;
		triggerConfOn.powerState = true;

		SwitchEventConfiguration triggerConfOff = new SwitchEventConfiguration();
		triggerConfOff.eventGeneratorName = triggerMainSwitch.name;
		triggerConfOff.powerState = false;

		roomSwitchProcess.eventTriggerConfigurations.add(triggerConfOn);
		roomSwitchProcess.eventTriggerConfigurations.add(triggerConfOff);

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

		NodeConfiguration grabCurrentSceneryNode = new NodeConfiguration();
		grabCurrentSceneryNode.id = 2;
		EventGeneratorSensorAdapterConfiguration grabSceneryHandler = new EventGeneratorSensorAdapterConfiguration();
		grabSceneryHandler.eventSensorId = "RoomSceneryEventGenerator";
		grabSceneryHandler.nextNodeId = 4;
		grabCurrentSceneryNode.actionHandler = grabSceneryHandler;
		roomSwitchProcess.nodes.put(2, grabCurrentSceneryNode);

		NodeConfiguration fireEventNode = new NodeConfiguration();
		fireEventNode.id = 4;
		FireEventHandlerConfiguration fireEventHandler = new FireEventHandlerConfiguration();
		fireEventHandler.event = null;
		fireEventHandler.nextNodeId = null;
		fireEventNode.actionHandler = fireEventHandler;
		roomSwitchProcess.nodes.put(4, fireEventNode);

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
		ArrayList<IUserRoomItem> turnLightOnFor = new ArrayList<IUserRoomItem>();
		turnLightOnFor.add(background);
		turnLightOnFor.add(sw1);
		turnLightOnFor.add(triggerMainSwitch);

		createUserScenario(rc, userScenario, changeConfigFor, turnLightOnFor);

		return rc;
	}


	private void createUserScenario(RoomConfiguration rc, UserSceneryConfiguration userScenario,
			List<LightObjectConfiguration> lo, List<IUserRoomItem> itemsToPutOn) {

		ProcessConfiguration process = new ProcessConfiguration();
		process.id = "process-scenario1";
		NodeConfiguration startNode = new NodeConfiguration();
		startNode.id = 0;

		SceneryEntryEventConfiguration triggerSceneryChange = new SceneryEntryEventConfiguration();
		triggerSceneryChange.sceneryName = "scenario1";
		triggerSceneryChange.eventGeneratorName = "RoomSceneryEventGenerator";
		process.eventTriggerConfigurations.add(triggerSceneryChange);

		ConfigurationChangeHandlerConfiguration cHandler = new ConfigurationChangeHandlerConfiguration();
		cHandler.nextNodeId = 1;
		for (LightObjectConfiguration current : lo) {
			cHandler.actorConfiguration.put(current.getName(), createSimpleColor());
		}
		startNode.actionHandler = cHandler;

		NodeConfiguration switchNode = new NodeConfiguration();
		switchNode.id = 1;

		PowerstateHandlerConfiguration powerstatehandler = new PowerstateHandlerConfiguration();
		powerstatehandler.nextNodeId = null;
		for (IUserRoomItem current : itemsToPutOn) {
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
