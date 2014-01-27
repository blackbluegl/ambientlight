package org.ambientlight.room;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.callback.CallBackManager;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.eventmanager.EventManager;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.process.ProcessManager;
import org.ambientlight.room.entities.alarm.AlarmManager;
import org.ambientlight.room.entities.climate.ClimateManager;
import org.ambientlight.room.entities.features.actor.Renderable;
import org.ambientlight.room.entities.features.actor.Switchable;
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


	public Map<String, Switchable> getSwitchableActors() {
		Map<String, Switchable> result = new HashMap<String, Switchable>();

		result.putAll(lightObjectManager.lightObjectConfigurations);

		result.putAll(switchesManager.switches);

		result.putAll(remoteSwitchesManager.remoteSwitches);

		return result;
	}


	public Map<String, Renderable> getRenderables() {

		Map<String, Renderable> result = new HashMap<String, Renderable>();

		result.putAll(lightObjectManager.lightObjectConfigurations);

		return result;
	}

}
