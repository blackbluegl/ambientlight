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

import org.ambientlight.config.device.drivers.RemoteHostConfiguration;
import org.ambientlight.messages.max.MaxDispatcher;


/**
 * @author Florian Bornkessel
 * 
 */
public class DispatcherManager {

	public void createDispatcher(final RemoteHostConfiguration config, final QeueManager queueManager) {

		final MaxDispatcher dispatcher = new MaxDispatcher();
		dispatcher.configuration = config;
		dispatcher.queueManager = queueManager;
		queueManager.registerOutDispatcher(DispatcherType.MAX, dispatcher);
		queueManager.registerOutDispatcher(DispatcherType.SYSTEM, dispatcher);

		connectDispatcher(dispatcher);
		startHeartBeatCheck(dispatcher, queueManager);

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (dispatcher instanceof InDispatcher) {
					startReceiveMessages(dispatcher, queueManager);
				}
			}
		}).start();
	}


	/**
	 * @param dispatcher
	 */
	private void connectDispatcher(MaxDispatcher dispatcher) {
		try {
			dispatcher.connect();
			System.out.println("DispatcherManager connectDispatcher(): Successfully connected Dispatcher: "
					+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
		} catch (Exception e) {
			System.out.println("DispatcherManager connectDispatcher(): could not connect Dispatcher: "
					+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName + ". Retrying.");
		}
	}


	private void startHeartBeatCheck(final Dispatcher dispatcher, final QeueManager queueManager) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						if (dispatcher.isConnected() == false) {
							System.out
							.println("DispatcherManager startHeartBeatCheck(): Connection lost. Reconnecting Dispatcher: "
									+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
							queueManager.dispatcherLostConnection(dispatcher.getDispatcherType());
							dispatcher.closeConnection();
							dispatcher.connect();
							queueManager.dispatcherRecoveredConnection(dispatcher.getDispatcherType());
							System.out.println("DispatcherManager startHeartBeatCheck(): Connection recovered for Dispatcher: "
									+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
						}

					} catch (Exception e) {
						System.out.println("DispatcherManager startHeartBeatCheck(): could not reconnect Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName
								+ ". Retrying");
					} finally {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}


	public synchronized boolean dispatchMessage(Dispatcher dispatcher, Message message) {
		return dispatcher.deliverMessage(message);
	}


	private void startReceiveMessages(final Dispatcher dispatcher, final QeueManager qeueManager) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {

					try {
						Message inMessage = ((MaxDispatcher) dispatcher).receiveMessages();

						if (inMessage != null) {
							qeueManager.putInMessage(inMessage);
						}
					} catch (Exception e) {

						System.out
						.println("DispatcherManager startReceiveMessages(): No connection. Awaiting reconnect for Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);

						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

}
