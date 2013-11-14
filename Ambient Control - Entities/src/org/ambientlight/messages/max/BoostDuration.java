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
public enum BoostDuration {

	_0(0), _5(5), _10(10), _15(15), _20(20), _25(25), _30(30), _60(60);

	public final int minutes;


	BoostDuration(int minutes) {
		this.minutes = minutes;
	}


	public static BoostDuration getClosest(int minutes) {
		BoostDuration closest = _0;
		int howClose = Integer.MAX_VALUE;
		for (BoostDuration bd : BoostDuration.values()) {
			int diff = Math.abs(bd.minutes - minutes);
			if (diff < howClose) {
				closest = bd;
				howClose = diff;
			}
		}

		return closest;
	}
}
