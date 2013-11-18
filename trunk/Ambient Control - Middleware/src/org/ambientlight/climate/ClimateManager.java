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

package org.ambientlight.climate;

import java.io.IOException;
import java.util.Date;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.MessageListener;
import org.ambientlight.messages.max.MaxMessage;
import org.ambientlight.messages.max.MaxThermostatStateMessage;
import org.ambientlight.room.ClimateConfiguration;
import org.ambientlight.room.RoomConfigurationFactory;
import org.ambientlight.room.entities.Thermostat;


/**
 * @author Florian Bornkessel
 * 
 */
public class ClimateManager implements MessageListener {

	public ClimateConfiguration config;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.MessageListener#handleMessage(org.ambientlight
	 * .messages.Message)
	 */
	@Override
	public void handleMessage(Message message) {
		if (message instanceof MaxMessage == false) {
			System.out.println("ClimateManager handleMessage(): Errror! Got unknown message: " + message);
			if (message instanceof MaxThermostatStateMessage) {
				handleThermostatState((MaxThermostatStateMessage) message);
			}
			return;
		}
	}


	/**
	 * @param message
	 * @throws IOException
	 */
	private void handleThermostatState(MaxThermostatStateMessage message) throws IOException {
		Thermostat thermostat = AmbientControlMW.getRoom().getThermostats().get(message.getFromAdress());
		if (thermostat == null) {
			System.out.println("ClimateManager handleThermostatState(): got request from unknown device: adress="
					+ message.getFromAdress());
			return;
		}
		thermostat.batteryLow = message.isBatteryLow();
		thermostat.isLocked = message.isLocked();
		thermostat.lastUpdate = new Date(System.currentTimeMillis());

		// TODO notify EventManager for new messured temperatur
		thermostat.temperatur = message.getActualTemp();

		config.mode = message.getMode();
		config.temporaryUntilDate = message.getTemporaryUntil();
		config.setTemp = message.getSetTemp();

		RoomConfigurationFactory.saveRoomConfiguration(AmbientControlMW.getRoom().config,
				AmbientControlMW.getRoomConfigFileName());
		AmbientControlMW.getRoom().callBackMananger.roomConfigurationChanged();
	}
}
