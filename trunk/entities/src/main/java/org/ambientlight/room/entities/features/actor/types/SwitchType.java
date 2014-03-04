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

package org.ambientlight.room.entities.features.actor.types;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ambientlight.events.types.SwitchEventType;


/**
 * @author Florian Bornkessel
 * 
 */
public enum SwitchType {
	VIRTUAL_MAIN(SwitchEventType.VIRTUAL_MAIN), VIRTUAL(SwitchEventType.VIRTUAL), NFC(SwitchEventType.NFC), ELRO(
			SwitchEventType.ELRO), LED(SwitchEventType.LED), ALARM(SwitchEventType.ALARM);

	public final SwitchEventType switchEventType;

	private static final Map<SwitchEventType, SwitchType> SWITCH_EVENT_TYPE_MAP = new LinkedHashMap<SwitchEventType, SwitchType>();

	static {
		for (SwitchType rae : SwitchType.values()) {
			SWITCH_EVENT_TYPE_MAP.put(rae.switchEventType, rae);
		}
	}


	SwitchType(SwitchEventType switchEventType) {
		this.switchEventType = switchEventType;
	}


	public static SwitchType forCode(SwitchEventType code) {
		return SWITCH_EVENT_TYPE_MAP.get(code);
	}

}
