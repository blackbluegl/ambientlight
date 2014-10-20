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

package org.ambientlight.room.entities.climate.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ambientlight.config.room.entities.climate.ClimateManagerConfiguration;
import org.ambientlight.config.room.entities.climate.DayEntry;
import org.ambientlight.config.room.entities.climate.MaxDayInWeek;
import org.ambientlight.rfmbridge.Message;
import org.ambientlight.rfmbridge.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.rfmbridge.messages.max.MaxConfigValveMessage;
import org.ambientlight.rfmbridge.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.rfmbridge.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.rfmbridge.messages.max.MaxFactoryResetMessage;
import org.ambientlight.rfmbridge.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.rfmbridge.messages.max.MaxSetGroupIdMessage;
import org.ambientlight.rfmbridge.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.rfmbridge.messages.max.MaxTimeInformationMessage;
import org.ambientlight.rfmbridge.messages.max.MaxConfigValveMessage.DecalcEntry;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxMessageCreator {

	private static int outSequenceNumber = 0;

	private ClimateManagerConfiguration config;


	public MaxMessageCreator(ClimateManagerConfiguration config) {
		this.config = config;
	}

	/**
	 * @param current
	 * @return
	 */
	public MaxSetTemperatureMessage getSetTempForDevice(int adress) {
		MaxSetTemperatureMessage outMessage = new MaxSetTemperatureMessage();
		outMessage.setFromAdress(config.vCubeAdress);
		// if (config.mode != MaxThermostateMode.AUTO) {
		outMessage.setTemp(config.temperature);
		// } else {
		// outMessage.setTemp(0.0f);
		// }
		outMessage.setTemporaryUntil(config.temporaryUntil);
		outMessage.setMode(config.mode);
		outMessage.setSequenceNumber(getNewSequnceNumber());
		outMessage.setToAdress(adress);
		outMessage.setGroupNumber(config.groupId);
		outMessage.setFlags(0x04);
		return outMessage;
	}


	/**
	 * @param fromAdress
	 * @param weekProfile
	 * @throws MessageMalFormedException
	 */
	public List<Message> getWeekProfileForDevice(Integer deviceAdress, String weekProfile) {
		List<Message> messages = new ArrayList<Message>();
		HashMap<MaxDayInWeek, List<DayEntry>> profiles = config.weekProfiles.get(weekProfile);
		for (Entry<MaxDayInWeek, List<DayEntry>> currentDayProfile : profiles.entrySet()) {
			int entryCountPartOne = currentDayProfile.getValue().size();
			boolean twoParts = false;

			if (entryCountPartOne > 7) {
				twoParts = true;
				entryCountPartOne = 7;
			}


			MaxConfigureWeekProgrammMessage week = new MaxConfigureWeekProgrammMessage();
			for (int i = 0; i < entryCountPartOne; i++) {
				week.addEntry(currentDayProfile.getValue().get(i));
			}

			week.setDay(currentDayProfile.getKey());
			week.setFromAdress(config.vCubeAdress);
			week.setSecondPart(false);
			week.setSequenceNumber(getNewSequnceNumber());
			week.setToAdress(deviceAdress);

			messages.add(week);

			if (twoParts) {
				MaxConfigureWeekProgrammMessage week2 = new MaxConfigureWeekProgrammMessage();
				for (int i = 7; i < currentDayProfile.getValue().size(); i++) {
					week2.addEntry(currentDayProfile.getValue().get(i));
				}


				week2.setDay(currentDayProfile.getKey());
				week2.setFromAdress(config.vCubeAdress);
				week2.setSecondPart(true);
				week2.setSecondPart(true);
				week2.setSequenceNumber(getNewSequnceNumber());
				week2.setToAdress(deviceAdress);
				messages.add(week2);
			}



		}
		return messages;
	}


	/**
	 * @param adress
	 * @return
	 */
	public MaxFactoryResetMessage getFactoryResetMessageForDevice(int adress) {
		MaxFactoryResetMessage resetDevice = new MaxFactoryResetMessage();
		resetDevice.setFromAdress(config.vCubeAdress);
		resetDevice.setToAdress(adress);
		resetDevice.setSequenceNumber(getNewSequnceNumber());
		return resetDevice;
	}


	/**
	 * @param device
	 * @param currentConfig
	 * @return
	 */
	public MaxRemoveLinkPartnerMessage getUnlinkMessageForDevice(int adress, int linkPartnerAdress,
			DeviceType linkPartnerDeviceType) {
		MaxRemoveLinkPartnerMessage unlink = new MaxRemoveLinkPartnerMessage();
		unlink.setFromAdress(config.vCubeAdress);
		unlink.setToAdress(adress);
		unlink.setSequenceNumber(getNewSequnceNumber());
		unlink.setLinkPartnerAdress(linkPartnerAdress);
		unlink.setLinkPartnerDeviceType(linkPartnerDeviceType);
		return unlink;
	}


	/**
	 * @param now
	 * @param current
	 * @return
	 */
	public MaxTimeInformationMessage getTimeInfoForDevice(Date now, int adress) {
		MaxTimeInformationMessage message = new MaxTimeInformationMessage();
		message.setSequenceNumber(getNewSequnceNumber());
		message.setFromAdress(config.vCubeAdress);
		message.setToAdress(adress);
		message.setTime(now);
		return message;
	}


	/**
	 * @param pairMessage
	 * @param config
	 * @param currentConfig
	 * @return
	 */
	public MaxAddLinkPartnerMessage getLinkMessage(int adress, int linkPartnerAdress, DeviceType linkPartnerTDeviceType) {
		MaxAddLinkPartnerMessage linkCurrentToNew = new MaxAddLinkPartnerMessage();
		linkCurrentToNew.setSequenceNumber(getNewSequnceNumber());
		linkCurrentToNew.setFromAdress(config.vCubeAdress);
		linkCurrentToNew.setToAdress(adress);
		linkCurrentToNew.setLinkPartnerAdress(linkPartnerAdress);
		linkCurrentToNew.setLinkPartnerDeviceType(linkPartnerTDeviceType);
		return linkCurrentToNew;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	public MaxSetGroupIdMessage getSetGroupIdForDevice(int adress) {
		MaxSetGroupIdMessage group = new MaxSetGroupIdMessage();
		group.setSequenceNumber(getNewSequnceNumber());
		group.setFromAdress(config.vCubeAdress);
		group.setToAdress(adress);
		group.setGroupId(config.groupId);
		return group;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	public MaxConfigureTemperaturesMessage getConfigureTemperatures(int adress) {
		MaxConfigureTemperaturesMessage temps = new MaxConfigureTemperaturesMessage();
		temps.setSequenceNumber(getNewSequnceNumber());
		temps.setFromAdress(config.vCubeAdress);
		temps.setToAdress(adress);
		temps.setComfortTemp(config.comfortTemperatur);
		temps.setEcoTemp(config.ecoTemperatur);
		temps.setMaxTemp(MaxUtil.MAX_TEMPERATURE);
		temps.setMinTemp(MaxUtil.MIN_TEMPERATURE);
		temps.setOffsetTemp(MaxUtil.DEFAULT_OFFSET);
		temps.setWindowOpenTemp(config.windowOpenTemperatur);
		temps.setWindowOpenTime(config.windowOpenTimeMins);
		return temps;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	public MaxConfigValveMessage getConfigValveForDevice(int adress) {
		MaxConfigValveMessage valve = new MaxConfigValveMessage();
		valve.setSequenceNumber(getNewSequnceNumber());
		valve.setFromAdress(config.vCubeAdress);
		valve.setToAdress(adress);
		valve.setBoostDuration(config.boostDurationMins);
		valve.setBoostValvePosition(config.boostValvePositionPercent);
		DecalcEntry decalc = valve.new DecalcEntry();
		decalc.day = config.decalcDay;
		decalc.hour = config.decalcHour;
		valve.setDecalc(decalc);
		valve.setMaxValvePosition(MaxUtil.DEFAULT_MAX_VALVE_POSITION);
		valve.setValveOffset(MaxUtil.DEFAULT_VALVE_OFFSET);
		return valve;
	}


	public synchronized int getNewSequnceNumber() {
		outSequenceNumber++;
		if (outSequenceNumber > 255) {
			outSequenceNumber = 0;
		}

		return outSequenceNumber;
	}
}
