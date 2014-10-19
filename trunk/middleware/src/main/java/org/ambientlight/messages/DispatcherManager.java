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

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.config.messages.DispatcherType;
import org.ambientlight.messages.max.MaxDispatcher;


/**
 * @author Florian Bornkessel
 * 
 */
public class DispatcherManager {

	QeueManager queueManager;

	Map<DispatcherType, Dispatcher> outDispatchers = new HashMap<DispatcherType, Dispatcher>();


	public DispatcherManager(QeueManager queueManager, Map<DispatcherType, Dispatcher> outDispatchers) {
		this.queueManager = queueManager;
		this.outDispatchers = outDispatchers;
	}


	public void startDispatchers() {

		for (final Dispatcher currentDispatcher : outDispatchers.values()) {
			connectDispatcher(currentDispatcher);

			if (currentDispatcher instanceof InDispatcher) {
				startReceiveMessages(currentDispatcher);
			}

			queueManager.onConnectDispatcher(DispatcherType.MAX);
			startHeartBeatCheck(currentDispatcher);
		}
	}


	/**
	 * @param dispatcher
	 */
	private void connectDispatcher(Dispatcher dispatcher) {
		try {
			dispatcher.connect();
			System.out.println("DispatcherManager connectDispatcher(): Successfully connected Dispatcher: "
					+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
		} catch (Exception e) {
			System.out.println("DispatcherManager connectDispatcher(): could not connect Dispatcher: "
					+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName + ". Retrying.");
		}
	}


	private void startHeartBeatCheck(final Dispatcher dispatcher) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						if (dispatcher.checkConnection() == false) {
							System.out
									.println("DispatcherManager startHeartBeatCheck(): Connection lost. Reconnecting Dispatcher: "
											+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
							queueManager.onDisconnectDispatcher(dispatcher.getDispatcherType());
							dispatcher.closeConnection();
							dispatcher.connect();
							queueManager.onConnectDispatcher(dispatcher.getDispatcherType());
							System.out.println("DispatcherManager startHeartBeatCheck(): Connection recovered for Dispatcher: "
									+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
						}

					} catch (Exception e) {
						System.out.println("DispatcherManager startHeartBeatCheck(): could not reconnect Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName
								+ ". Retrying");
					} finally {
						try {
							Thread.sleep(12000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}


	public synchronized boolean dispatchMessage(Message message) {
		Dispatcher dispatcher = this.outDispatchers.get(message.getDispatcherType());
		return dispatcher.deliverMessage(message);
	}


	private void startReceiveMessages(final Dispatcher dispatcher) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {

					try {
						Message inMessage = ((MaxDispatcher) dispatcher).receiveMessages();

						if (inMessage != null) {
							queueManager.putInMessage(inMessage);
						}
					} catch (Exception e) {

						System.out
								.println("DispatcherManager startReceiveMessages(): No connection. Awaiting reconnect for Dispatcher: "
										+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);

						try {
							Thread.sleep(2500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}).start();
	}
}
