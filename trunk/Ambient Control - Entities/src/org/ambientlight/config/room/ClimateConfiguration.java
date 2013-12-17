/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.ambientlight.config.room;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.config.room.actors.MaxComponentConfiguration;
import org.ambientlight.messages.max.DayEntry;
import org.ambientlight.messages.max.MaxConfigValveMessage;
import org.ambientlight.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxThermostateMode;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateConfiguration {

	public final float DEFAULT_OFFSET = MaxConfigureTemperaturesMessage.DEFAULT_OFFSET;
	public final int DEFAULT_MAX_VALVE_POSITION = MaxConfigValveMessage.DEFAULT_MAX_VALVE_POSITION;
	public final int DEFAULT_VALVE_OFFSET_PERCENT = 0;

	public MaxThermostateMode mode = MaxThermostateMode.AUTO;
	public float setTemp = 22.0f;
	public Date temporaryUntilDate;
	public boolean windowOpen = false;
	public String currentWeekProfile = "default";

	public int vCubeAdress = 1;
	public int proxyShutterContactAdress = 2;
	public int groupId = 5;

	public float comfortTemperatur = 21.0f;
	public float ecoTemperatur = 17.0f;
	public float maxTemp = MaxConfigureTemperaturesMessage.MAX_TEMPERATURE;
	public float minTemp = MaxConfigureTemperaturesMessage.MIN_TEMPERATURE;
	public float windowOpenTemperatur = 12.0f;
	public int windowOpenTimeMins = 15;
	public int boostDurationMins = 60;
	public int boostValvePositionPercent = 100;
	public int decalcHour = 12;
	public MaxDayInWeek decalcDay = MaxDayInWeek.SATURDAY;

	public Map<String, HashMap<MaxDayInWeek, List<DayEntry>>> weekProfiles = new HashMap<String, HashMap<MaxDayInWeek, List<DayEntry>>>();
	public Map<Integer, MaxComponentConfiguration> devices = new HashMap<Integer, MaxComponentConfiguration>();
}
