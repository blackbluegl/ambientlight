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

package org.ambientlight.room;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage.DayEntry;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxThermostateMode;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateConfiguration {

	public int vCubeAdress;
	public int groupId;
	public MaxThermostateMode mode;
	public Date temporaryUntilDate;
	public float setTemp;
	public float comfortTemperatur = 21.0f;
	public float ecoTemperatur = 17.0f;
	public float windowOpenTemperatur = 12.0f;
	public int windowOpenTimeMins = 15;
	public int boostDurationMins = 60;
	public int boostValvePositionPercent = 100;
	public int valveOffsetPercent = 0;

	public boolean windowOpen = false;

	public Map<String, HashMap<MaxDayInWeek, List<DayEntry>>> weekProfiles = new HashMap<String, HashMap<MaxDayInWeek, List<DayEntry>>>();
}
