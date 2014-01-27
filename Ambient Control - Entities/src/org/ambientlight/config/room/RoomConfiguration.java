package org.ambientlight.config.room;

import java.io.Serializable;

import org.ambientlight.config.process.ProcessManagerConfiguration;
import org.ambientlight.config.room.entities.alarm.AlarmManagerConfiguration;
import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.lightobject.LightObjectManagerConfiguration;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitchManagerConfiguration;
import org.ambientlight.config.room.entities.scenery.SceneryManagerConfiguration;
import org.ambientlight.config.room.entities.switches.SwitchManagerConfiguration;
import org.ambientlight.messages.QeueManagerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("room")
public class RoomConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public String roomName;

	public QeueManagerConfiguration qeueManager;
	public ClimateManagerConfiguration climateManager;
	public SwitchManagerConfiguration switchesManager;
	public RemoteSwitchManagerConfiguration remoteSwitchesManager;
	public AlarmManagerConfiguration alarmManager;
	public SceneryManagerConfiguration sceneriesManager;
	public ProcessManagerConfiguration processManager;
	public LightObjectManagerConfiguration lightObjectManager;
}
