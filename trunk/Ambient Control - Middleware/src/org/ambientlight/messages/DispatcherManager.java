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

import org.ambientlight.config.device.drivers.MaxVCubeDeviceConfiguration;
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

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (config instanceof MaxVCubeDeviceConfiguration) {

					connectDispatcher(dispatcher);

					if (dispatcher instanceof InDispatcher) {
						startReceiveMessages(dispatcher, queueManager);
					}

					startHeartBeatCheck(dispatcher);
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
		} catch (Exception e) {

			System.out.println("DispatcherManager connectDispatcher(): could not connect Dispatcher: "
					+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName + ". Retrying.");
			boolean connected = false;
			while (connected == false) {
				try {
					Thread.sleep(10000);
					connected = dispatcher.reconnect();
					if (!connected) {
						System.out.println("DispatcherManager connectDispatcher(): could not reconnect Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName
								+ ". Retrying.");
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		System.out.println("DispatcherManager connectDispatcher(): Successfully connected Dispatcher: "
				+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
	}


	private void startHeartBeatCheck(final Dispatcher dispatcher) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
						dispatcher.sendLock.lock();
						if (dispatcher.isConnected() == false) {
							System.out
							.println("DispatcherManager startHeartBeatCheck(): Connection lost. Reconnecting Dispatcher: "
									+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);

							dispatcher.closeConnection();
							dispatcher.connect();
							System.out.println("DispatcherManager startHeartBeatCheck(): Connection recovered for Dispatcher: "
									+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
						}

					} catch (Exception e) {
						System.out.println("DispatcherManager startHeartBeatCheck(): could not reconnect Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName
								+ ". Retrying");
					} finally {
						dispatcher.sendLock.unlock();
					}
				}
			}
		}).start();
	}


	public boolean dispatchMessage(Dispatcher dispatcher, Message message) {
		dispatcher.sendLock.lock();
		try {
			return dispatcher.deliverMessage(message);
		} catch (Exception e) {

		} finally {
			dispatcher.sendLock.unlock();
		}
		return false;
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
						.println("DispatcherManager startReceiveMessages(): No connection. Awaiting reconnect. Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

}
