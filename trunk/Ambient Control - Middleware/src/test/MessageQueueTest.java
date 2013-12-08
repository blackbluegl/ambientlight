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

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.device.drivers.MaxVCubeDeviceConfiguration;
import org.ambientlight.messages.DispatcherManager;
import org.ambientlight.messages.DispatcherType;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.QeueManager;
import org.ambientlight.messages.max.DeviceType;
import org.ambientlight.messages.max.MaxAddLinkPartnerMessage;
import org.ambientlight.messages.max.MaxMessageType;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxThermostateMode;
import org.ambientlight.messages.max.MaxWakeUpMessage;
import org.ambientlight.messages.max.WaitForShutterContactCondition;


/**
 * @author Florian Bornkessel
 * 
 */
public class MessageQueueTest {

	public static void main(String[] args) throws InterruptedException {
		QeueManager manager = new QeueManager();
		MessageDump dump = new MessageDump();
		manager.registerMessageListener(DispatcherType.MAX, dump);

		MaxVCubeDeviceConfiguration config = new MaxVCubeDeviceConfiguration();
		config.hostName = "ambi-schlafen";
		config.port = 30000;
		DispatcherManager df = new DispatcherManager();
		manager.dispatcherManager = df;
		df.createDispatcher(config, manager);
		manager.startQeues();

		MaxSetTemperatureMessage tempMsg = new MaxSetTemperatureMessage();
		tempMsg.setFlags(0x5);
		tempMsg.setFromAdress(167874);
		tempMsg.setToAdress(431563);
		// tempMsg.setGroupNumber(1);
		tempMsg.setMessageType(MaxMessageType.SET_TEMPERATURE);
		tempMsg.setMode(MaxThermostateMode.AUTO);
		tempMsg.setSequenceNumber(37);
		tempMsg.setTemp(22.5f);

		ArrayList<Message> out = new ArrayList<Message>();
		out.add(tempMsg);
		// Thread.sleep(1500);
		// manager.putOutMessages(out);
		//
		// for (int i = 0; i < 10000; i++) {
		// TestMessage test = new TestMessage();
		// manager.putOutMessage(test);
		// // Thread.sleep(1);
		// // System.out.println(i);
		// }
		MaxPairPongMessage pairPong = new MaxPairPongMessage();
		pairPong.setSequenceNumber(1);
		pairPong.setFromAdress(1);
		pairPong.setToAdress(529299);

		MaxWakeUpMessage wakeUp = new MaxWakeUpMessage();
		wakeUp.setFromAdress(1);
		wakeUp.setToAdress(529299);
		wakeUp.setFlags(MaxWakeUpMessage.FLAGS_NONE);
		wakeUp.setSequenceNumber(2);

		MaxWakeUpMessage wakeUp2 = new MaxWakeUpMessage();
		wakeUp2.setFromAdress(1);
		wakeUp2.setToAdress(537069);
		wakeUp2.setFlags(MaxWakeUpMessage.FLAGS_NONE);
		wakeUp2.setSequenceNumber(3);

		// byte[] payloadTest = wakeUp.getPayload();
		// byte[] p2 = new byte[12];
		// for (int i = 0; i < 11; i++) {
		// p2[i] = payloadTest[i];
		// }
		// wakeUp.setPayload(p2);

		MaxPairPongMessage pairPong1 = new MaxPairPongMessage();
		pairPong.setSequenceNumber(3);
		pairPong.setFromAdress(1);
		pairPong.setToAdress(529299);

		MaxAddLinkPartnerMessage link = new MaxAddLinkPartnerMessage();
		link.setFromAdress(1);
		link.setToAdress(537069);
		link.setSequenceNumber(4);
		link.setLinkPartnerAdress(529299);
		link.setLinkPartnerDeviceType(DeviceType.SHUTTER_CONTACT);

		MaxAddLinkPartnerMessage link2 = new MaxAddLinkPartnerMessage();
		link2.setFromAdress(1);
		link2.setToAdress(529299);
		link2.setSequenceNumber(5);
		link2.setLinkPartnerAdress(537069);
		link2.setLinkPartnerDeviceType(DeviceType.HEATING_THERMOSTAT);

		MaxAddLinkPartnerMessage link3 = new MaxAddLinkPartnerMessage();
		link3.setFromAdress(1);
		link3.setToAdress(529299);
		link3.setSequenceNumber(52);
		link3.setLinkPartnerAdress(537069);
		link3.setLinkPartnerDeviceType(DeviceType.HEATING_THERMOSTAT);

		MaxAddLinkPartnerMessage link4 = new MaxAddLinkPartnerMessage();
		link4.setFromAdress(1);
		link4.setToAdress(529299);
		link4.setSequenceNumber(55);
		link4.setLinkPartnerAdress(537069);
		link4.setLinkPartnerDeviceType(DeviceType.HEATING_THERMOSTAT);

		MaxSetTemperatureMessage temp = new MaxSetTemperatureMessage();
		temp.setSequenceNumber(22);
		temp.setFromAdress(1);
		temp.setToAdress(537069);
		temp.setMode(MaxThermostateMode.MANUAL);

		// manager.putOutMessage(temp);
		// manager.putOutMessage(pairPong);
		// manager.putOutMessage(wakeUp);
		// manager.putOutMessage(pairPong1);
		// manager.putOutMessage(link);
		// manager.putOutMessage(link2);
		List<Message> outMessages = new ArrayList<Message>();
		// outMessages.add(temp);
		// outMessages.add(pairPong);
		// outMessages.add(wakeUp);
		// outMessages.add(link);
		// outMessages.add(link2);

		WaitForShutterContactCondition condition = new WaitForShutterContactCondition(529299);
		manager.putOutMessage(wakeUp, condition);
		manager.putOutMessage(wakeUp2, condition);
		manager.putOutMessage(link2, condition);
		manager.putOutMessage(link3, condition);
		manager.putOutMessage(link4, condition);
		System.out.println("finished");
	}
}
