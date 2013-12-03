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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.ambientlight.messages.Dispatcher;
import org.ambientlight.messages.InDispatcher;
import org.ambientlight.messages.Message;
import org.ambientlight.messages.rfm22bridge.PingMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxDispatcher extends Dispatcher implements InDispatcher {

	ReentrantLock sendLock = new ReentrantLock();

	private boolean pongReceived = false;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.OutDispatcher#deliverMessage(org.ambientlight
	 * .messages.Message)
	 */
	@Override
	public synchronized boolean deliverMessage(Message message) {
		try {

			String header = message.getCommand() + "|" + message.getDispatcherType() + "|";
			if (message.getValue() != null) {
				header = header + message.getValue() + "|";
			}
			header = header + "\n";

			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.print(header);
			ps.flush();
			if (message instanceof MaxMessage) {
				socket.getOutputStream().write(getByteStreamFromMaxMessage((MaxMessage) message));
				socket.getOutputStream().flush();
			}
		} catch (Exception e) {
			System.out.println("MaxDispatcher deliverMessage(): was unable to send data!");
			return false;
		}
		return true;
	}


	@Override
	public Message receiveMessages() throws IOException {
		InputStream in = socket.getInputStream();
		String line = readLine(in);

		String[] command = line.split("\\|");

		if (command.length != 2)
			return null;

		int length = Integer.parseInt(command[1]);
		byte[] messageBytes = new byte[length];
		in.read(messageBytes, 0, length);
		return parseMessage(command[0], messageBytes);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.InDispatcher#parseMessage(java.util.ArrayList)
	 */
	@Override
	public Message parseMessage(String dispatcherType, byte[] input) {

		// handle system messages
		if ("SYSTEM".equals(dispatcherType)) {
			char[] chars = new char[input.length];
			for (int i = 0; i < input.length; i++) {
				chars[i] = (char) input[i];
			}
			String command = new String(chars);
			if ("PONG".equals(command)) {
				pongReceived = true;
			}
			return null;
		}

		// handle max messages
		MaxMessage message = new MaxMessage();
		message.setPayload(input);

		MaxMessage result = new MaxMessage();
		switch (message.getMessageType()) {
		case THERMOSTAT_STATE:
			result = new MaxThermostatStateMessage();
			break;
		case SET_TEMPERATURE:
			result = new MaxSetTemperatureMessage();
			break;
		case ACK:
			result = new MaxAckMessage();
			break;
		case TIME_INFORMATION:
			result = new MaxTimeInformationMessage();
			break;
		case CONFIG_WEEK_PROFILE:
			result = new MaxConfigureWeekProgrammMessage();
			break;
		case CONFIG_VALVE:
			result = new MaxConfigValveMessage();
			break;
		case CONFIG_TEMPERATURES:
			result = new MaxConfigureTemperaturesMessage();
			break;
		case PAIR_PING:
			result = new MaxPairPingMessage();
			break;
		case PAIR_PONG:
			result = new MaxPairPongMessage();
			break;
		case SET_GROUP_ID:
			result = new MaxSetGroupIdMessage();
			break;
		case REMOVE_GROUP_ID:
			result = new MaxRemoveGroupIdMessage();
			break;
		case SHUTTER_CONTACT_STATE:
			result = new MaxShutterContactStateMessage();
			break;
		case WAKE_UP:
			result = new MaxWakeUpMessage();
			break;
		case RESET:
			result = new MaxFactoryResetMessage();
			break;
		case ADD_LINK_PARTNER:
			result = new MaxAddLinkPartnerMessage();
			break;
		case REMOVE_LINK_PARTNER:
			result = new MaxRemoveLinkPartnerMessage();
			break;
		default:
			result = new MaxMessage();
		}

		result.setPayload(input);

		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ambientlight.messages.OutDispatcher#isConnected()
	 */
	@Override
	public boolean isConnected() {

		pongReceived = false;
		try {
			PingMessage ping = new PingMessage();


			deliverMessage(ping);

			Thread.sleep(3000);
			System.out.println("ping");
			if (pongReceived) {
				System.out.println("pong");
				return true;
			}

		} catch (Exception e) {
			System.out.println("pong ex");
			return false;
		}
		System.out.println("no pong");
		return false;
	}


	byte[] getByteStreamFromMaxMessage(MaxMessage message) {
		return message.getPayload();
	}


	private String readLine(InputStream inputStream) throws IOException {
		ArrayList<Character> result = new ArrayList<Character>();

		boolean finished = false;
		while (!finished) {
			int charAt = inputStream.read();
			if (charAt == -1) {
				finished = true;
			} else if ('\n' == (char) charAt) {
				finished = true;
			} else {
				result.add((char) charAt);
			}
		}

		String resultString = new String();
		for (char charAt : result) {
			resultString += charAt;
		}

		return resultString;
	}

}
