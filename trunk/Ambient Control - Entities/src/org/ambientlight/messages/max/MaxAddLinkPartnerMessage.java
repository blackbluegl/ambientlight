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

import org.ambientlight.messages.AckRequestMessage;


/**
 * 
 * @author Florian Bornkessel
 * 
 */
public class MaxAddLinkPartnerMessage extends MaxMessage implements AckRequestMessage {

	public MaxAddLinkPartnerMessage() {
		payload = new byte[14];
		setMessageType(MaxMessageType.ADD_LINK_PARTNER);
		setFlags(FLAGS_NONE);
	}


	public Integer getLinkPartnerAdress() {
		return MaxUtil.byteArrayToInt(payload, 10, 3);
	}


	public void setLinkPartnerAdress(int adress) {
		byte[] result = MaxUtil.intToByteArray(adress);
		payload[10] = result[1];
		payload[11] = result[2];
		payload[12] = result[3];
	}


	public void setLinkPartnerDeviceType(DeviceType type) {
		payload[13] = type.byteValue;
	}


	public DeviceType getLinkPartnerType() {
		return DeviceType.forCode(payload[13]);
	}


	@Override
	public String toString() {
		String parent = super.toString();
		String current = "linkPartner: " + getLinkPartnerAdress() + "\nPartnerType: " + getLinkPartnerType();
		return parent + "\n" + current;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getTimeOutSec()
	 */
	@Override
	public int getTimeOutSec() {
		return 10;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getRetryCount()
	 */
	@Override
	public int getRetryCount() {
		// TODO Auto-generated method stub
		return 5;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.AckRequestMessage#getCorrelation()
	 */
	@Override
	public String getCorrelation() {
		// TODO Auto-generated method stub
		return String.valueOf(getSequenceNumber());
	}
}
