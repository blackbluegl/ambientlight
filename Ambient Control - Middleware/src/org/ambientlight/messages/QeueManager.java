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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author Florian Bornkessel
 * 
 */
public class QeueManager {

	public DispatcherManager dispatcherManager;

	HashMap<DispatcherType, Dispatcher> outDispatchers = new HashMap<DispatcherType, Dispatcher>();
	HashMap<DispatcherType, MessageListener> messageListeners = new HashMap<DispatcherType, MessageListener>();

	ArrayList<Message> outQueue = new ArrayList<Message>();
	ArrayList<Message> inQeue = new ArrayList<Message>();

	ReentrantLock outLock = new ReentrantLock();
	Condition hasOutMessages = outLock.newCondition();

	ReentrantLock inLock = new ReentrantLock();
	Condition hasInMessages = inLock.newCondition();


	public void startQeues() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					outLock.lock();
					try {
						hasOutMessages.await();
						if (!handleOutMessages()) {
							retryHandleOutMessages();
						}
					} catch (InterruptedException e) {
					} finally {
						outLock.unlock();
					}
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {

					inLock.lock();
					try {
						hasInMessages.await();

						for (Message inMessage : inQeue) {
							MessageListener listener = messageListeners.get(inMessage.getDispatcherType());
							if (listener != null) {
								listener.handleMessage(inMessage);
							}
						}
						inQeue.clear();

					} catch (InterruptedException e) {
					} finally {
						inLock.unlock();
					}
				}
			}
		}).start();
	}


	public void registerOutDispatcher(DispatcherType dispatcherType, Dispatcher dispatcher) {
		outDispatchers.put(dispatcherType, dispatcher);
	}


	public void registerMessageListener(DispatcherType dispatcherType, MessageListener listener) {
		messageListeners.put(dispatcherType, listener);
	}


	public void putOutMessages(List<Message> messages) {
		outLock.lock();
		outQueue.addAll(messages);
		hasOutMessages.signal();
		outLock.unlock();
	}


	public void putInMessage(Message message) {
		inLock.lock();
		inQeue.add(message);
		hasInMessages.signal();
		inLock.unlock();
	}


	private boolean handleOutMessages() {

		List<Message> messagesToRemove = new ArrayList<Message>();
		for (Message outMessage : outQueue) {

			if (dispatcherManager.dispatchMessage(outDispatchers.get(outMessage.getDispatcherType()), outMessage)) {
				messagesToRemove.add(outMessage);
			}
		}
		outQueue.removeAll(messagesToRemove);
		if (outQueue.isEmpty() == false)
			return false;
		else
			return true;
	}


	private void retryHandleOutMessages() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				hasOutMessages.signal();
			}
		}).start();
	}
}
