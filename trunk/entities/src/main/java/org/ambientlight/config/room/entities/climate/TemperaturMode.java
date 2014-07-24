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

package org.ambientlight.config.room.entities.climate;

import java.util.Date;

import org.ambientlight.room.entities.climate.util.MaxThermostateMode;


/**
 * @author Florian Bornkessel
 * 
 */
public class TemperaturMode {

	public TemperaturMode(float temp, Date until, MaxThermostateMode mode) {
		super();
		this.temp = temp;
		this.until = until;
		this.thermostateMode = mode;
	}

	public float temp;
	public Date until;
	public MaxThermostateMode thermostateMode;
}
