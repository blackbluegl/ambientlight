package org.ambientlight.room;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.events.EventManager;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.process.ProcessManager;
import org.ambientlight.room.entities.EntitiesFacade;
import org.ambientlight.room.entities.alarm.AlarmManager;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.lightobject.LightObjectManager;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitchManager;
import org.ambientlight.room.entities.sceneries.SceneryManager;
import org.ambientlight.room.entities.switches.SwitchManager;


/**
 * 
 * @author florian
 * 
 */
public class Room {

	public EntitiesFacade entitiesFacade;

	public LightObjectManager lightObjectManager;

	public ClimateManager climateManager;

	public SwitchManager schwitchManager;

	public RemoteSwitchManager remoteSwitchManager;

	public QeueManager qeueManager;

	public SceneryManager sceneryManager;

	public AlarmManager alarmManager;

	public CallBackManager callBackMananger;

	public EventManager eventManager;

	public ProcessManager processManager;

	public RoomConfiguration config;

}
