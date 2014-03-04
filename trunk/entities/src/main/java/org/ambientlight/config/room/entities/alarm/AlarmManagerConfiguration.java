package org.ambientlight.config.room.entities.alarm;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.room.entities.alarm.DailyAlarm;


public class AlarmManagerConfiguration {

	public Map<String, DailyAlarm> alarms = new HashMap<String, DailyAlarm>();
}
