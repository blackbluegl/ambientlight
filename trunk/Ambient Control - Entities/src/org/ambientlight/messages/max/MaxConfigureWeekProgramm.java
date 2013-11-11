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

package org.ambientlight.messages.max;

/**
 * @author Florian Bornkessel
 * 
 */
public class MaxConfigureWeekProgramm extends MaxMessage {

	public MaxConfigureWeekProgramm() {
		setFlags(FLAGS_NONE);
		payload = new byte[25];// 23 for second part
	}


	public MaxDayInWeek getDayIndWeek() {
		return (MaxDayInWeek.forCode(payload[10]));
	}


	public void setDay(MaxDayInWeek day) {
		payload[10] = day.byteValue;
	}


}
