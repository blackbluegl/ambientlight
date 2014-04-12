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

package org.ambientlight.process.handler;

import java.text.DecimalFormat;

import org.ambientlight.room.entities.features.sensor.ScenerySensor;
import org.ambientlight.room.entities.features.sensor.Sensor;
import org.ambientlight.room.entities.features.sensor.SwitchSensor;
import org.ambientlight.room.entities.features.sensor.TemperatureSensor;
import org.ambientlight.room.entities.sceneries.Scenery;


/**
 * @author Florian Bornkessel
 * 
 */
public class Util {

	public String getDataFromSensor(Sensor sensor) {
		if (sensor instanceof TemperatureSensor)
			return new DecimalFormat("#.##").format(sensor.getSensorValue());
		else if (sensor instanceof ScenerySensor)
			return ((Scenery) sensor.getSensorValue()).id;
		else if (sensor instanceof SwitchSensor) {
			sensor.getSensorValue();
			return ((Boolean) sensor.getSensorValue()) == true ? "1" : "0";
		}
		return "0.0";
	}
}
