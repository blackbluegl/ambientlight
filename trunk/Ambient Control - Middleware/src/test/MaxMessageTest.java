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

package test;

import java.util.Calendar;

import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxThermostateMode;


/**
 * @author Florian Bornkessel
 *
 */
public class MaxMessageTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MaxSetTemperatureMessage temp = new MaxSetTemperatureMessage();
		temp.setMode(MaxThermostateMode.TEMPORARY);
		temp.setTemp(32f);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 21);
		cal.set(Calendar.HOUR_OF_DAY, 19);
		cal.set(Calendar.MINUTE, 30);
		temp.setTemporaryUntil(cal.getTime());

		System.out.println(temp);
	}

}
