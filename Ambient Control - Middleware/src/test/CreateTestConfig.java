package test;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.config.device.drivers.DummyLedStripeDeviceConfiguration;
import org.ambientlight.config.device.drivers.DummyRemoteSwitchBridgeConfiguration;
import org.ambientlight.config.device.led.ColorConfiguration;
import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.config.device.led.StripePartConfiguration;
import org.ambientlight.config.messages.DispatcherConfiguration;
import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.config.messages.QeueManagerConfiguration;
import org.ambientlight.config.process.EventProcessConfiguration;
import org.ambientlight.config.process.NodeConfiguration;
import org.ambientlight.config.process.ProcessManagerConfiguration;
import org.ambientlight.config.process.handler.actor.PowerstateHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.RenderingProgrammChangeHandlerConfiguration;
import org.ambientlight.config.process.handler.actor.SimplePowerStateHandlerConfiguration;
import org.ambientlight.config.process.handler.event.EventToBooleanHandlerConfiguration;
import org.ambientlight.config.process.handler.event.FireEventHandlerConfiguration;
import org.ambientlight.config.process.handler.event.SensorToTokenConfiguration;
import org.ambientlight.config.process.handler.expression.DecisionHandlerConfiguration;
import org.ambientlight.config.process.handler.expression.ExpressionConfiguration;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.climate.DayEntry;
import org.ambientlight.config.room.entities.climate.MaxDayInWeek;
import org.ambientlight.config.room.entities.lightobject.LightObjectManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.RenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitchManagerConfiguration;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.events.SceneryEntryEvent;
import org.ambientlight.events.SwitchEvent;
import org.ambientlight.events.types.SwitchEventType;
import org.ambientlight.room.Persistence;
import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.ambientlight.room.entities.features.actor.types.SwitchableId;
import org.ambientlight.room.entities.lightobject.LightObject;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitch;
import org.ambientlight.room.entities.sceneries.Scenery;
import org.ambientlight.room.entities.sceneries.SceneryManager;
import org.ambientlight.room.entities.switches.Switch;


public class CreateTestConfig {

	public static String VIRTUAL_MAIN_SWITCH_ID = "main";
	public static String LO_BACKGROUND_ID = "background";
	public static String LO_LO1_ID = "lightObject1";
	public static String REMOTE_SWITCH_1 = "remoteSwitch1";
	public static String SCENERY_SCENERY1 = "Scenario1";
	public static String SCENERY_SCENERY2 = "Scenario2";


	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Persistence.beginTransaction();

		CreateTestConfig test = new CreateTestConfig();

		Persistence.commitTransaction(test.getTestRoom(), "default");
	}


	public RoomConfiguration getTestRoom() {
		RoomConfiguration rc = new RoomConfiguration();

		rc.roomName = "testRoom";

		createSceneryManager(rc);

		createRemoteSwitchManager(rc);

		createSwitchManager(rc);

		createClimate(rc);

		createLightObjectManager(rc);

		createProcessManagerWithMainSwitch(rc);
		createProcessWithSceneryForProcessManager(rc);

		return rc;
	}


	/**
	 * @param rc
	 */
	private void createSwitchManager(RoomConfiguration rc) {
		SwitchManagerConfiguration config = new SwitchManagerConfiguration();
		Switch mainSwitch = new Switch();
		mainSwitch.setType(SwitchType.VIRTUAL_MAIN);
		mainSwitch.setId(VIRTUAL_MAIN_SWITCH_ID);
		mainSwitch.setPowerState(false);
		config.switches.put(mainSwitch.getId(), mainSwitch);
		rc.switchesManager = config;
	}


	private void createProcessManagerWithMainSwitch(RoomConfiguration rc) {

		ProcessManagerConfiguration config = new ProcessManagerConfiguration();
		rc.processManager = config;

		EventProcessConfiguration roomSwitchProcess = new EventProcessConfiguration();
		roomSwitchProcess.run = true;
		roomSwitchProcess.id = "roomSwitchProcess";

		config.processes.put(roomSwitchProcess.id, roomSwitchProcess);

		SwitchEvent triggerOn = new SwitchEvent(VIRTUAL_MAIN_SWITCH_ID, true, SwitchEventType.VIRTUAL_MAIN);
		SwitchEvent triggerOff = new SwitchEvent(VIRTUAL_MAIN_SWITCH_ID, false, SwitchEventType.VIRTUAL_MAIN);

		roomSwitchProcess.eventTriggerConfigurations.add(triggerOn);
		roomSwitchProcess.eventTriggerConfigurations.add(triggerOff);

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
		SensorToTokenConfiguration grabSceneryHandler = new SensorToTokenConfiguration();
		grabSceneryHandler.sensorId = "SCENERY:SceneryManager";
		grabCurrentSceneryNode.nextNodeIds.add(4);
		grabCurrentSceneryNode.actionHandler = grabSceneryHandler;
		roomSwitchProcess.nodes.put(2, grabCurrentSceneryNode);

		NodeConfiguration fireEventNode = new NodeConfiguration();
		fireEventNode.id = 4;
		FireEventHandlerConfiguration fireEventHandler = new FireEventHandlerConfiguration();
		fireEventHandler.event = new SceneryEntryEvent(SceneryManager.SENSOR_NAME, "#{tokenValue}");
		fireEventNode.actionHandler = fireEventHandler;
		roomSwitchProcess.nodes.put(4, fireEventNode);

		NodeConfiguration turnOffNode = new NodeConfiguration();
		turnOffNode.id = 3;
		SimplePowerStateHandlerConfiguration powerDownHandler = new SimplePowerStateHandlerConfiguration();
		powerDownHandler.powerState = false;
		turnOffNode.actionHandler = powerDownHandler;
		roomSwitchProcess.nodes.put(3, turnOffNode);
	}


	/**
	 * @param rc
	 */
	private void createSceneryManager(RoomConfiguration rc) {
		SceneryManagerConfiguration config = new SceneryManagerConfiguration();
		rc.sceneriesManager = config;
		Scenery userScenario = new Scenery();
		userScenario.id = CreateTestConfig.SCENERY_SCENERY1;
		config.currentScenery = userScenario;
		config.sceneries.put(userScenario.id, userScenario);

		Scenery userScenario2 = new Scenery();
		userScenario2.id = CreateTestConfig.SCENERY_SCENERY2;
		config.sceneries.put(userScenario2.id, userScenario2);
	}


	/**
	 * @param rc
	 */
	private void createRemoteSwitchManager(RoomConfiguration rc) {

		DummyRemoteSwitchBridgeConfiguration switchingBridge = new DummyRemoteSwitchBridgeConfiguration();
		// SwitchDeviceOverEthernetConfiguration switchingBridge = new
		// SwitchDeviceOverEthernetConfiguration();
		// switchingBridge.hostName = "rfmbridge";
		// switchingBridge.port = 2003;

		RemoteSwitchManagerConfiguration config = new RemoteSwitchManagerConfiguration();
		rc.remoteSwitchesManager = config;
		config.device = switchingBridge;

		RemoteSwitch sw1 = new RemoteSwitch();
		sw1.houseCode = 15;
		sw1.switchingUnitCode = 3;
		sw1.setId(CreateTestConfig.REMOTE_SWITCH_1);
		sw1.setPowerState(false);

		config.remoteSwitches.put(sw1.getId(), sw1);

	}


	/**
	 * @param rc
	 */
	private void createLightObjectManager(RoomConfiguration rc) {

		LightObjectManagerConfiguration config = new LightObjectManagerConfiguration();
		rc.lightObjectManager = config;
		config.height = 400;
		config.width = 400;

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

		DummyLedStripeDeviceConfiguration dc = new DummyLedStripeDeviceConfiguration();
		// MultiStripeOverEthernetClientDeviceConfiguration dc = new
		// MultiStripeOverEthernetClientDeviceConfiguration();
		// dc.hostName = "ambi-schlafen";
		// dc.port = 2002;

		config.devices.add(dc);

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

		sc.protocollType = StripeConfiguration.PROTOCOLL_TYPE_DIRECT_SPI;
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

		LightObject lo = new LightObject();
		lo.setId(LO_LO1_ID);
		lo.height = 20;
		lo.layerNumber = 2;
		lo.width = 20;
		lo.xOffsetInRoom = 0;
		lo.yOffsetInRoom = 0;
		lo.setRenderingProgrammConfiguration(this.createSimpleColor());
		config.lightObjects.put(lo.getId(), lo);

		LightObject background = new LightObject();
		background.setPowerState(true);
		background.setId(LO_BACKGROUND_ID);
		background.height = 200;
		background.layerNumber = 1;
		background.width = 200;
		background.xOffsetInRoom = 0;
		background.yOffsetInRoom = 0;
		SunSetRenderingProgrammConfiguration sunset = new SunSetRenderingProgrammConfiguration();
		background.setRenderingProgrammConfiguration(sunset);
		config.lightObjects.put(background.getId(), background);
	}


	/**
	 * @param rc
	 * @return
	 */
	private void createClimate(RoomConfiguration rc) {
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

		DispatcherConfiguration dispatcherConfig = new DispatcherConfiguration();

		dispatcherConfig.hostName = "ambi-schlafen";
		dispatcherConfig.port = 30000;
		dispatcherConfig.type = DispatcherType.MAX;

		QeueManagerConfiguration qConfig = new QeueManagerConfiguration();
		qConfig.dispatchers.add(dispatcherConfig);
		rc.qeueManager = qConfig;
	}


	private void createProcessWithSceneryForProcessManager(RoomConfiguration rc) {

		Map<String, RenderingProgramConfiguration> changeConfigFor = new HashMap<String, RenderingProgramConfiguration>();
		changeConfigFor.put(CreateTestConfig.LO_BACKGROUND_ID, createSimpleColor());
		changeConfigFor.put(CreateTestConfig.LO_LO1_ID, createSimpleColor());

		Map<SwitchableId, Boolean> turnOnLightFor = new HashMap<SwitchableId, Boolean>();
		turnOnLightFor.put(new SwitchableId(LO_BACKGROUND_ID, SwitchType.LED), true);
		turnOnLightFor.put(new SwitchableId(LO_LO1_ID, SwitchType.LED), true);
		turnOnLightFor.put(new SwitchableId(VIRTUAL_MAIN_SWITCH_ID, SwitchType.VIRTUAL_MAIN), true);

		EventProcessConfiguration process = new EventProcessConfiguration();
		process.run = true;
		process.id = "process-scenario1";
		NodeConfiguration startNode = new NodeConfiguration();
		startNode.id = 0;

		SceneryEntryEvent triggerSceneryChange = new SceneryEntryEvent(SceneryManager.SENSOR_NAME,
				CreateTestConfig.SCENERY_SCENERY1);
		process.eventTriggerConfigurations.add(triggerSceneryChange);

		RenderingProgrammChangeHandlerConfiguration cHandler = new RenderingProgrammChangeHandlerConfiguration();
		cHandler.renderConfig = changeConfigFor;
		startNode.actionHandler = cHandler;
		startNode.nextNodeIds.add(1);

		NodeConfiguration switchNode = new NodeConfiguration();
		switchNode.id = 1;

		PowerstateHandlerConfiguration powerstatehandler = new PowerstateHandlerConfiguration();
		powerstatehandler.powerStateConfiguration = turnOnLightFor;
		switchNode.actionHandler = powerstatehandler;

		process.nodes.put(0, startNode);
		process.nodes.put(1, switchNode);

		rc.processManager.processes.put(process.id, process);
	}


	private SimpleColorRenderingProgramConfiguration createSimpleColor() {

		SimpleColorRenderingProgramConfiguration scL01 = new SimpleColorRenderingProgramConfiguration();
		int i = (int) (Math.random() * 256);

		Color color = new Color(i, 10, 100);
		scL01.rgb = color.getRGB();
		return scL01;
	}
}
