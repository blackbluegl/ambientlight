package test;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.config.device.drivers.DummyLedStripeDeviceConfiguration;
import org.ambientlight.config.device.drivers.DummySwitchDeviceConfiguration;
import org.ambientlight.config.device.drivers.MaxVCubeDeviceConfiguration;
import org.ambientlight.config.device.led.ColorConfiguration;
import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.config.device.led.StripePartConfiguration;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.config.process.events.SceneryEntryEvent;
import org.ambientlight.config.process.events.SwitchEvent;
import org.ambientlight.config.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.config.process.handler.event.EventGeneratorSensorAdapterConfiguration;
import org.ambientlight.config.process.handler.event.EventToBooleanHandlerConfiguration;
import org.ambientlight.config.process.handler.event.FireEventHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.ExpressionConfiguration;
import org.ambientlight.config.room.ClimateConfiguration;
import org.ambientlight.config.room.ISwitchableRoomItem;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.actors.LightObjectConfiguration;
import org.ambientlight.config.room.actors.SwitchObjectConfiguration;
import org.ambientlight.config.room.eventgenerator.SceneryEventGeneratorConfiguration;
import org.ambientlight.config.room.eventgenerator.SwitchEventGeneratorConfiguration;
import org.ambientlight.config.scenery.UserSceneryConfiguration;
import org.ambientlight.config.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.scenery.actor.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.scenery.actor.switching.SwitchingConfiguration;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.messages.max.DayEntry;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.room.RoomConfigurationFactory;


public class CreateTestConfig {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		RoomConfigurationFactory.beginTransaction();
		DeviceDriverFactory df = new DeviceDriverFactory();
		CreateTestConfig test = new CreateTestConfig();

		RoomConfigurationFactory.commitTransaction(test.getTestRoom(), "default");
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

		ClimateConfiguration climate = new ClimateConfiguration();
		climate.vCubeAdress = 1;
		climate.groupId = 5;

		List<DayEntry> mon = new ArrayList<DayEntry>();
		DayEntry mon1 = new DayEntry(24, 0, 22.0f);
		mon.add(mon1);

		List<DayEntry> tue = new ArrayList<DayEntry>();
		DayEntry tue1 = new DayEntry(24, 0, 22.0f);
		tue.add(tue1);

		List<DayEntry> wed = new ArrayList<DayEntry>();
		DayEntry wed1 = new DayEntry(24, 0, 22.0f);
		wed.add(wed1);

		List<DayEntry> thu = new ArrayList<DayEntry>();
		DayEntry thu1 = new DayEntry(24, 0, 22.0f);
		thu.add(thu1);

		List<DayEntry> fri = new ArrayList<DayEntry>();
		DayEntry fri1 = new DayEntry(24, 0, 22.0f);
		fri.add(fri1);

		List<DayEntry> sat = new ArrayList<DayEntry>();
		DayEntry sat1 = new DayEntry(24, 0, 22.0f);
		sat.add(sat1);

		List<DayEntry> sun = new ArrayList<DayEntry>();
		DayEntry sun1 = new DayEntry(24, 0, 22.0f);
		sun.add(sun1);
		HashMap<MaxDayInWeek, List<DayEntry>> weekProfile = new HashMap<MaxDayInWeek, List<DayEntry>>();
		weekProfile.put(MaxDayInWeek.MONDAY, mon);
		weekProfile.put(MaxDayInWeek.TUESDAY, tue);
		weekProfile.put(MaxDayInWeek.WEDNESDAY, wed);
		weekProfile.put(MaxDayInWeek.THURSDAY, thu);
		weekProfile.put(MaxDayInWeek.FRIDAY, fri);
		weekProfile.put(MaxDayInWeek.SATURDAY, sat);
		weekProfile.put(MaxDayInWeek.SUNDAY, sun);

		Map<String, HashMap<MaxDayInWeek, List<DayEntry>>> weekProfiles = new HashMap<String, HashMap<MaxDayInWeek, List<DayEntry>>>();
		weekProfiles.put("default", weekProfile);
		climate.weekProfiles = weekProfiles;
		climate.currentWeekProfile = "default";

		rc.climate = climate;

		MaxVCubeDeviceConfiguration rfmConfig = new MaxVCubeDeviceConfiguration();

		rfmConfig.hostName = "ambi-schlafen";
		rfmConfig.port = 30000;

		rc.deviceConfigurations.add(rfmConfig);
		rc.deviceConfigurations.add(switchingBridge);
		rc.deviceConfigurations.add(dc);

		float value = 1.0f;
		float gamma = 1.0f;
		ColorConfiguration cConfig = new ColorConfiguration();
		cConfig.gammaRed = gamma;
		cConfig.gammaGreen = gamma;
		cConfig.gammaBlue = gamma;
		cConfig.levelRed = value;
		cConfig.levelBlue = value;
		cConfig.levelGreen = value;

		StripeConfiguration sc = new StripeConfiguration();
		sc.colorConfiguration = cConfig;

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
		lo.xOffsetInRoom = 0;
		lo.yOffsetInRoom = 0;
		lo.actorConductConfiguration = this.createSimpleColor();
		rc.actorConfigurations.put(lo.getName(), lo);

		LightObjectConfiguration background = new LightObjectConfiguration();
		background.setPowerState(true);
		background.setName("background");
		background.height = 200;
		background.layerNumber = 1;
		background.width = 200;
		background.xOffsetInRoom = 0;
		background.yOffsetInRoom = 0;
		SunSetRenderingProgrammConfiguration sunset = new SunSetRenderingProgrammConfiguration();
		background.actorConductConfiguration = sunset;
		rc.actorConfigurations.put(background.getName(), background);

		SwitchEventGeneratorConfiguration triggerMainSwitch = new SwitchEventGeneratorConfiguration();
		triggerMainSwitch.name = "RoomSwitch";
		rc.eventGeneratorConfigurations.put("RoomSwitch", triggerMainSwitch);

		SceneryEventGeneratorConfiguration sceneryEventGenerator = new SceneryEventGeneratorConfiguration();
		sceneryEventGenerator.name = "RoomSceneryEventGenerator";
		rc.eventGeneratorConfigurations.put("RoomSceneryEventGenerator", sceneryEventGenerator);

		rc.height = 400;
		rc.width = 400;
		rc.roomName = "testRoom";

		UserSceneryConfiguration userScenario = new UserSceneryConfiguration();
		userScenario.id = "scenario1";
		sceneryEventGenerator.sceneries.add(userScenario);

		UserSceneryConfiguration userScenario2 = new UserSceneryConfiguration();
		userScenario2.id = "scenario2";
		sceneryEventGenerator.sceneries.add(userScenario2);

		sceneryEventGenerator.currentScenery = userScenario;

		EventProcessConfiguration roomSwitchProcess = new EventProcessConfiguration();
		roomSwitchProcess.run = true;
		roomSwitchProcess.id = "roomSwitch";
		rc.processes.add(roomSwitchProcess);
		SwitchEvent triggerConfOn = new SwitchEvent();
		triggerConfOn.sourceName = triggerMainSwitch.name;
		triggerConfOn.powerState = true;

		SwitchEvent triggerConfOff = new SwitchEvent();
		triggerConfOff.sourceName = triggerMainSwitch.name;
		triggerConfOff.powerState = false;

		roomSwitchProcess.eventTriggerConfigurations.add(triggerConfOn);
		roomSwitchProcess.eventTriggerConfigurations.add(triggerConfOff);

		NodeConfiguration eventMapperNode = new NodeConfiguration();
		eventMapperNode.id = 0;
		EventToBooleanHandlerConfiguration eventMapper = new EventToBooleanHandlerConfiguration();
		eventMapperNode.nextNodeIds.add(1);
		eventMapperNode.actionHandler = eventMapper;
		roomSwitchProcess.nodes.put(0, eventMapperNode);

		NodeConfiguration decissionNode = new NodeConfiguration();
		decissionNode.id = 1;
		DecisionHandlerConfiguration decission = new DecisionHandlerConfiguration();
		decissionNode.nextNodeIds.add(2);
		decissionNode.nextNodeIds.add(3);
		ExpressionConfiguration expression = new ExpressionConfiguration();
		decission.expressionConfiguration = expression;
		expression.expression = "#{tokenValue}==1.0";
		decissionNode.actionHandler = decission;
		roomSwitchProcess.nodes.put(1, decissionNode);

		NodeConfiguration grabCurrentSceneryNode = new NodeConfiguration();
		grabCurrentSceneryNode.id = 2;
		EventGeneratorSensorAdapterConfiguration grabSceneryHandler = new EventGeneratorSensorAdapterConfiguration();
		grabSceneryHandler.eventSensorId = "RoomSceneryEventGenerator";
		grabCurrentSceneryNode.nextNodeIds.add(4);
		grabCurrentSceneryNode.actionHandler = grabSceneryHandler;
		roomSwitchProcess.nodes.put(2, grabCurrentSceneryNode);

		NodeConfiguration fireEventNode = new NodeConfiguration();
		fireEventNode.id = 4;
		FireEventHandlerConfiguration fireEventHandler = new FireEventHandlerConfiguration();
		fireEventHandler.event = null;
		fireEventNode.actionHandler = fireEventHandler;
		roomSwitchProcess.nodes.put(4, fireEventNode);

		NodeConfiguration turnOffNode = new NodeConfiguration();
		turnOffNode.id = 3;
		SimplePowerStateHandlerConfiguration powerDownHandler = new SimplePowerStateHandlerConfiguration();
		powerDownHandler.powerState = false;
		turnOffNode.actionHandler = powerDownHandler;
		roomSwitchProcess.nodes.put(3, turnOffNode);

		ArrayList<LightObjectConfiguration> changeConfigFor = new ArrayList<LightObjectConfiguration>();
		changeConfigFor.add(background);
		changeConfigFor.add(lo);
		ArrayList<ISwitchableRoomItem> turnLightOnFor = new ArrayList<ISwitchableRoomItem>();
		turnLightOnFor.add(background);
		turnLightOnFor.add(sw1);
		turnLightOnFor.add(triggerMainSwitch);

		createUserScenario(rc, userScenario, changeConfigFor, turnLightOnFor);

		return rc;
	}


	private void createUserScenario(RoomConfiguration rc, UserSceneryConfiguration userScenario,
			List<LightObjectConfiguration> lo, List<ISwitchableRoomItem> itemsToPutOn) {

		EventProcessConfiguration process = new EventProcessConfiguration();
		process.run = true;
		process.id = "process-scenario1";
		NodeConfiguration startNode = new NodeConfiguration();
		startNode.id = 0;

		SceneryEntryEvent triggerSceneryChange = new SceneryEntryEvent();
		triggerSceneryChange.sceneryName = "scenario1";
		triggerSceneryChange.sourceName = "RoomSceneryEventGenerator";
		process.eventTriggerConfigurations.add(triggerSceneryChange);

		ConfigurationChangeHandlerConfiguration cHandler = new ConfigurationChangeHandlerConfiguration();
		for (LightObjectConfiguration current : lo) {
			cHandler.actorConfiguration.put(current.getName(), createSimpleColor());
		}
		startNode.actionHandler = cHandler;
		startNode.nextNodeIds.add(1);

		NodeConfiguration switchNode = new NodeConfiguration();
		switchNode.id = 1;

		PowerstateHandlerConfiguration powerstatehandler = new PowerstateHandlerConfiguration();
		for (ISwitchableRoomItem current : itemsToPutOn) {
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
