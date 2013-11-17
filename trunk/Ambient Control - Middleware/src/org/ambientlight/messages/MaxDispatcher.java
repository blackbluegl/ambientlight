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

package org.ambientlight.messages;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.ambientlight.messages.max.MaxAckMessage;
import org.ambientlight.messages.max.MaxConfigValveMessage;
import org.ambientlight.messages.max.MaxConfigureTemperaturesMessage;
import org.ambientlight.messages.max.MaxConfigureWeekProgrammMessage;
import org.ambientlight.messages.max.MaxMessage;
import org.ambientlight.messages.max.MaxPairPingMessage;
import org.ambientlight.messages.max.MaxPairPongMessage;
import org.ambientlight.messages.max.MaxSetTemperatureMessage;
import org.ambientlight.messages.max.MaxThermostatStateMessage;
import org.ambientlight.messages.max.MaxTimeInformationMessage;


/**
 * @author Florian Bornkessel
 * 
 */
public class MaxDispatcher extends Dispatcher implements InDispatcher {

	private boolean pongReceived = false;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.OutDispatcher#deliverMessage(org.ambientlight
	 * .messages.Message)
	 */
	@Override
	public boolean deliverMessage(Message message) {
		try {
			String header = "RFM_SEND_MESSAGE|" + message.getDispatcherType() + "|" + ((MaxMessage) message).getPayload().length
					+ "|\n";
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.print(header);
			socket.getOutputStream().write(getByteStreamFromMaxMessage((MaxMessage) message));
			socket.getOutputStream().flush();
		} catch (Exception e) {
			System.out.println("MaxDispatcher deliverMessage(): was unable to send data!");
			reconnect();
			return false;
		}
		return true;
	}


	@Override
	public Message receiveMessages() throws IOException {

		String line = readLine();

		if ("PONG".equals(line)) {
			pongReceived = true;
			return null;
		}

		String[] command = line.split("\\|");

		if (command.length != 2)
			return null;

		if ("MAX".equals(command[0])) {
			int length = Integer.parseInt(command[1]);
			byte[] messageBytes = new byte[length];
			socket.getInputStream().read(messageBytes, 0, length);
			return parseMessage(messageBytes);
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.messages.InDispatcher#parseMessage(java.util.ArrayList)
	 */
	@Override
	public Message parseMessage(byte[] input) {
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
			socket.getOutputStream().write("PING|\n".getBytes());
			socket.getOutputStream().flush();
			Thread.sleep(3000);
			if (pongReceived)
				return true;
		} catch (Exception e) {
			return false;
		}
		return false;
	}


	byte[] getByteStreamFromMaxMessage(MaxMessage message) {
		return message.getPayload();
	}


	private String readLine() throws IOException {
		ArrayList<Character> result = new ArrayList<Character>();

		boolean finished = false;
		while (!finished) {
			int charAt = socket.getInputStream().read();
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
