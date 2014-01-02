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

import org.ambientlight.AmbientControlMW;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.max.DayEntry;
import org.ambientlight.messages.max.DeviceType;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxConfigValveMessage;
import org.ambientlight.messages.max.MaxConfigValveMessage.DecalcEntry;
import org.ambientlight.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.messages.max.MaxDayInWeek;
import org.ambientlight.messages.max.MaxFactoryResetMessage;
import org.ambientlight.messages.max.MaxRemoveLinkPartnerMessage;
import org.ambientlight.messages.max.MaxSetGroupIdMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxTimeInformationMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxMessageCreator {

	private static int outSequenceNumber = 0;


	/**
	 * @param current
	 * @return
	 */
	public static MaxSetTemperatureMessage getSetTempForDevice(int adress) {
		MaxSetTemperatureMessage outMessage = new MaxSetTemperatureMessage();
		outMessage.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		if (AmbientControlMW.getRoom().config.climateManager.mode != MaxThermostateMode.AUTO) {
			outMessage.setTemp(AmbientControlMW.getRoom().config.climateManager.setTemp);
		} else {
			outMessage.setTemp(0.0f);
		}
		outMessage.setTemporaryUntil(AmbientControlMW.getRoom().config.climateManager.temporaryUntilDate);
		outMessage.setMode(AmbientControlMW.getRoom().config.climateManager.mode);
		outMessage.setSequenceNumber(getNewSequnceNumber());
		outMessage.setToAdress(adress);
		outMessage.setGroupNumber(AmbientControlMW.getRoom().config.climateManager.groupId);
		outMessage.setFlags(0x04);
		return outMessage;
	}


	/**
	 * @param fromAdress
	 * @param weekProfile
	 * @throws MessageMalFormedException
	 */
	public static List<Message> getWeekProfileForDevice(Integer deviceAdress, String weekProfile) {
		List<Message> messages = new ArrayList<Message>();
		HashMap<MaxDayInWeek, List<DayEntry>> profiles = AmbientControlMW.getRoom().config.climateManager.weekProfiles.get(weekProfile);
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
			week.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
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
				week2.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
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
	public static MaxFactoryResetMessage getFactoryResetMessageForDevice(int adress) {
		MaxFactoryResetMessage resetDevice = new MaxFactoryResetMessage();
		resetDevice.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		resetDevice.setToAdress(adress);
		resetDevice.setSequenceNumber(getNewSequnceNumber());
		return resetDevice;
	}


	/**
	 * @param device
	 * @param currentConfig
	 * @return
	 */
	public static MaxRemoveLinkPartnerMessage getUnlinkMessageForDevice(int adress, int linkPartnerAdress,
			DeviceType linkPartnerDeviceType) {
		MaxRemoveLinkPartnerMessage unlink = new MaxRemoveLinkPartnerMessage();
		unlink.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
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
	public static MaxTimeInformationMessage getTimeInfoForDevice(Date now, int adress) {
		MaxTimeInformationMessage message = new MaxTimeInformationMessage();
		message.setSequenceNumber(getNewSequnceNumber());
		message.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
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
	public static MaxAddLinkPartnerMessage getLinkMessage(int adress, int linkPartnerAdress, DeviceType linkPartnerTDeviceType) {
		MaxAddLinkPartnerMessage linkCurrentToNew = new MaxAddLinkPartnerMessage();
		linkCurrentToNew.setSequenceNumber(getNewSequnceNumber());
		linkCurrentToNew.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		linkCurrentToNew.setToAdress(adress);
		linkCurrentToNew.setLinkPartnerAdress(linkPartnerAdress);
		linkCurrentToNew.setLinkPartnerDeviceType(linkPartnerTDeviceType);
		return linkCurrentToNew;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	public static MaxSetGroupIdMessage getSetGroupIdForDevice(int adress) {
		MaxSetGroupIdMessage group = new MaxSetGroupIdMessage();
		group.setSequenceNumber(getNewSequnceNumber());
		group.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		group.setToAdress(adress);
		group.setGroupId(AmbientControlMW.getRoom().config.climateManager.groupId);
		return group;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	public static MaxConfigureTemperaturesMessage getConfigureTemperatures(int adress) {
		MaxConfigureTemperaturesMessage temps = new MaxConfigureTemperaturesMessage();
		temps.setSequenceNumber(getNewSequnceNumber());
		temps.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		temps.setToAdress(adress);
		temps.setComfortTemp(AmbientControlMW.getRoom().config.climateManager.comfortTemperatur);
		temps.setEcoTemp(AmbientControlMW.getRoom().config.climateManager.ecoTemperatur);
		temps.setMaxTemp(AmbientControlMW.getRoom().config.climateManager.maxTemp);
		temps.setMinTemp(AmbientControlMW.getRoom().config.climateManager.minTemp);
		temps.setOffsetTemp(AmbientControlMW.getRoom().config.climateManager.DEFAULT_OFFSET);
		temps.setWindowOpenTemp(AmbientControlMW.getRoom().config.climateManager.windowOpenTemperatur);
		temps.setWindowOpenTime(AmbientControlMW.getRoom().config.climateManager.windowOpenTimeMins);
		return temps;
	}


	/**
	 * @param pairMessage
	 * @return
	 */
	public static MaxConfigValveMessage getConfigValveForDevice(int adress) {
		MaxConfigValveMessage valve = new MaxConfigValveMessage();
		valve.setSequenceNumber(getNewSequnceNumber());
		valve.setFromAdress(AmbientControlMW.getRoom().config.climateManager.vCubeAdress);
		valve.setToAdress(adress);
		valve.setBoostDuration(AmbientControlMW.getRoom().config.climateManager.boostDurationMins);
		valve.setBoostValvePosition(AmbientControlMW.getRoom().config.climateManager.boostValvePositionPercent);
		DecalcEntry decalc = valve.new DecalcEntry();
		decalc.day = AmbientControlMW.getRoom().config.climateManager.decalcDay;
		decalc.hour = AmbientControlMW.getRoom().config.climateManager.decalcHour;
		valve.setDecalc(decalc);
		valve.setMaxValvePosition(AmbientControlMW.getRoom().config.climateManager.DEFAULT_MAX_VALVE_POSITION);
		valve.setValveOffset(AmbientControlMW.getRoom().config.climateManager.DEFAULT_VALVE_OFFSET_PERCENT);
		return valve;
	}


	public static synchronized int getNewSequnceNumber() {
		outSequenceNumber++;
		if (outSequenceNumber > 255) {
			outSequenceNumber = 0;
		}

		return outSequenceNumber;
	}
}
