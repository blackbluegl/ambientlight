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
import org.ambientlight.config.events.SceneryEntryEvent;
import org.ambientlight.config.events.SwitchEvent;
import org.ambientlight.config.features.actor.Switchable;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.config.process.handler.actor.ConfigurationChangeHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.config.process.handler.event.EventGeneratorSensorAdapterConfiguration;
import org.ambientlight.config.process.handler.event.EventToBooleanHandlerConfiguration;
import org.ambientlight.config.process.handler.event.FireEventHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.ExpressionConfiguration;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.switching.SwitchingConfiguration;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.config.room.entities.scenery.Scenery;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.messages.max.DayEntry;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitch;


public class CreateTestConfig {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Persistence.beginTransaction();
		DeviceDriverFactory df = new DeviceDriverFactory();
		CreateTestConfig test = new CreateTestConfig();

		Persistence.commitTransaction(test.getTestRoom(), "default");
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

		ClimateManagerConfiguration climate = new ClimateManagerConfiguration();
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

		rc.climateManager = climate;

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

		RemoteSwitch sw1 = new RemoteSwitch();
		sw1.deviceType = "ELRO";
		sw1.houseCode = 15;
		sw1.switchingUnitCode = 3;
		sw1.setId("kleine Stehlampe");
		sw1.actorConductConfiguration = new SwitchingConfiguration();
		rc.lightObjectConfigurations.put(sw1.getId(), sw1);

		LightObjectConfiguration lo = new LightObjectConfiguration();
		lo.setId("Schrank");
		lo.height = 20;
		lo.layerNumber = 2;
		lo.width = 20;
		lo.xOffsetInRoom = 0;
		lo.yOffsetInRoom = 0;
		lo.actorConductConfiguration = this.createSimpleColor();
		rc.lightObjectConfigurations.put(lo.getId(), lo);

		LightObjectConfiguration background = new LightObjectConfiguration();
		background.setPowerState(true);
		background.setId("background");
		background.height = 200;
		background.layerNumber = 1;
		background.width = 200;
		background.xOffsetInRoom = 0;
		background.yOffsetInRoom = 0;
		SunSetRenderingProgrammConfiguration sunset = new SunSetRenderingProgrammConfiguration();
		background.actorConductConfiguration = sunset;
		rc.lightObjectConfigurations.put(background.getId(), background);

		SwitchManagerConfiguration triggerMainSwitch = new SwitchManagerConfiguration();
		triggerMainSwitch.name = "RoomSwitch";
		rc.eventGeneratorConfigurations.put("RoomSwitch", triggerMainSwitch);

		SceneryManagerConfiguration sceneryEventGenerator = new SceneryManagerConfiguration();
		sceneryEventGenerator.name = "RoomSceneryEventGenerator";
		rc.eventGeneratorConfigurations.put("RoomSceneryEventGenerator", sceneryEventGenerator);

		rc.height = 400;
		rc.width = 400;
		rc.roomName = "testRoom";

		Scenery userScenario = new Scenery();
		userScenario.id = "scenario1";
		sceneryEventGenerator.sceneries.add(userScenario);

		Scenery userScenario2 = new Scenery();
		userScenario2.id = "scenario2";
		sceneryEventGenerator.sceneries.add(userScenario2);

		sceneryEventGenerator.currentScenery = userScenario;

		EventProcessConfiguration roomSwitchProcess = new EventProcessConfiguration();
		roomSwitchProcess.run = true;
		roomSwitchProcess.id = "roomSwitch";
		rc.processes.add(roomSwitchProcess);
		SwitchEvent triggerConfOn = new SwitchEvent();
		triggerConfOn.sourceId = triggerMainSwitch.name;
		triggerConfOn.powerState = true;

		SwitchEvent triggerConfOff = new SwitchEvent();
		triggerConfOff.sourceId = triggerMainSwitch.name;
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
		ArrayList<Switchable> turnLightOnFor = new ArrayList<Switchable>();
		turnLightOnFor.add(background);
		turnLightOnFor.add(sw1);
		turnLightOnFor.add(triggerMainSwitch);

		createUserScenario(rc, userScenario, changeConfigFor, turnLightOnFor);

		return rc;
	}


	private void createUserScenario(RoomConfiguration rc, Scenery userScenario,
			List<LightObjectConfiguration> lo, List<Switchable> itemsToPutOn) {

		EventProcessConfiguration process = new EventProcessConfiguration();
		process.run = true;
		process.id = "process-scenario1";
		NodeConfiguration startNode = new NodeConfiguration();
		startNode.id = 0;

		SceneryEntryEvent triggerSceneryChange = new SceneryEntryEvent();
		triggerSceneryChange.sceneryName = "scenario1";
		triggerSceneryChange.sourceId = "RoomSceneryEventGenerator";
		process.eventTriggerConfigurations.add(triggerSceneryChange);

		ConfigurationChangeHandlerConfiguration cHandler = new ConfigurationChangeHandlerConfiguration();
		for (LightObjectConfiguration current : lo) {
			cHandler.actorConfiguration.put(current.getId(), createSimpleColor());
		}
		startNode.actionHandler = cHandler;
		startNode.nextNodeIds.add(1);

		NodeConfiguration switchNode = new NodeConfiguration();
		switchNode.id = 1;

		PowerstateHandlerConfiguration powerstatehandler = new PowerstateHandlerConfiguration();
		for (Switchable current : itemsToPutOn) {
			powerstatehandler.powerStateConfiguration.put(current.getId(), true);
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
