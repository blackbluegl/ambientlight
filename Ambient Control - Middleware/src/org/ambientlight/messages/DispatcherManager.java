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

import org.ambientlight.device.drivers.MaxVCubeDeviceConfiguration;
import org.ambientlight.device.drivers.RemoteHostConfiguration;


/**
 * @author Florian Bornkessel
 * 
 */
public class DispatcherManager {


	public void createDispatcher(final RemoteHostConfiguration config, final QeueManager queueManager) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (config instanceof MaxVCubeDeviceConfiguration) {
					MaxDispatcher dispatcher = new MaxDispatcher();
					dispatcher.configuration = config;
					dispatcher.queueManager = queueManager;

					connectDispatcher(dispatcher);

					queueManager.registerOutDispatcher(DispatcherType.MAX, dispatcher);

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
					+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
			e.printStackTrace();
			boolean connected = false;
			while (connected == false) {
				try {
					Thread.sleep(10000);
					connected = dispatcher.reconnect();
					if (!connected) {
						System.out.println("DispatcherManager connectDispatcher(): could not reconnect Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
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
						dispatcher.sendLock.lock();

						System.out.println("DispatcherManager startReceiveMessages(): Connection lost. Reconnecting Dispatcher: "
								+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);

						if (dispatcher.reconnect()) {
							System.out.println("DispatcherManager startReceiveMessages(): Connection recovered. Dispatcher: "
									+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
							dispatcher.sendLock.lock();
						} else {
							System.out
							.println("DispatcherManager startReceiveMessages(): Connection could not recovered. Waiting for Heartbeat. Dispatcher: "
									+ dispatcher.getClass().getSimpleName() + " to: " + dispatcher.configuration.hostName);
							dispatcher.sendLock.lock();
						}

						dispatcher.sendLock.unlock();

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