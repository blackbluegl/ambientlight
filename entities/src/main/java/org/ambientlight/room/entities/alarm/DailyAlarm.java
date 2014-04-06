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

package org.ambientlight.room.entities.alarm;

import org.ambientlight.room.entities.features.EntityId;



/**
 * @author Florian Bornkessel
 * 
 */
public class DailyAlarm extends Alarm {

	private static final long serialVersionUID = 1L;

	public int hour;
	public int minute;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.room.entities.features.Entity#getId()
	 */
	@Override
	public EntityId getId() {
		return new EntityId(EntityId.DOMAIN_ALARM_DAILY, id);
	}

}
