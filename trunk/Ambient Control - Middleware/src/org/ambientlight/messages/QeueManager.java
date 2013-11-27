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

	public enum State {
		PENDING, SENDING, SENT, WAITING_FOR_CONDITION, AWAITING_RESPONSE, SEND_ERROR, TIMED_OUT, RETRIEVED_ANSWER;
	}

	private class MessageEntry {

		WaitForResponseCondition condition;
		Message message;
		State state = State.PENDING;
	}

	public DispatcherManager dispatcherManager;

	HashMap<DispatcherType, Dispatcher> outDispatchers = new HashMap<DispatcherType, Dispatcher>();
	HashMap<DispatcherType, MessageListener> messageListeners = new HashMap<DispatcherType, MessageListener>();

	ArrayList<MessageEntry> outQueue = new ArrayList<MessageEntry>();
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
						handleInMessages();
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
		if (messages == null || messages.size() == 0)
			return;

		outLock.lock();
		List<MessageEntry> entries = new ArrayList<QeueManager.MessageEntry>();
		for (Message current : messages) {
			MessageEntry entry = new MessageEntry();
			entry.message = current;
			entries.add(entry);
		}
		outQueue.addAll(entries);
		hasOutMessages.signal();
		outLock.unlock();
	}


	public void putOutMessage(Message message) {
		putOutMessage(message, null);
	}


	public void putOutMessage(Message message, WaitForResponseCondition waitCondition) {
		if (message == null)
			return;

		MessageEntry entry = new MessageEntry();
		entry.message = message;
		entry.condition = waitCondition;
		if (waitCondition != null) {
			entry.state = State.WAITING_FOR_CONDITION;
		}
		outLock.lock();
		outQueue.add(entry);
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
		boolean sendFailureOccured = false;
		outLock.lock();
		List<MessageEntry> messagesToRemove = new ArrayList<MessageEntry>();
		for (MessageEntry out : outQueue) {
			if (out.state == State.AWAITING_RESPONSE || out.state == State.WAITING_FOR_CONDITION) {
				continue;
			}
			out.state = State.SENDING;
			if (dispatcherManager.dispatchMessage(outDispatchers.get(out.message.getDispatcherType()), out.message)) {
				if (out.message instanceof AckRequestMessage) {
					out.state = State.AWAITING_RESPONSE;
				} else {
					out.state = State.SENT;
					messagesToRemove.add(out);
				}
			} else {
				out.state = State.SEND_ERROR;
				sendFailureOccured = true;
			}

		}
		outQueue.removeAll(messagesToRemove);
		outLock.unlock();

		this.startWatchDogForWaitingMessages();

		// if an error while sending occoured we have to retry sending again
		if (sendFailureOccured)
			return false;
		else
			return true;
	}


	private void handleInMessages() {
		for (Message inMessage : inQeue) {
			MessageListener listener = messageListeners.get(inMessage.getDispatcherType());
			if (listener == null) {
				continue;
			}

			handleConditionalOutMessageForResponse(inMessage);

			if (inMessage instanceof AckResponseMessage) {
				// there maybe an outMessage waiting for
				// this message
				MessageEntry foundRequest = getAwaitingRequestMessageForInMessage(inMessage, listener);
				if (foundRequest != null) {
					foundRequest.state = State.RETRIEVED_ANSWER;
					listener.handleResponseMessages(foundRequest.state, inMessage, foundRequest.message);
				}
			} else {
				listener.handleMessage(inMessage);
			}
		}
		inQeue.clear();
	}


	private void startWatchDogForWaitingMessages() {
		outLock.lock();
		for (final MessageEntry current : outQueue) {
			if (current.state == State.AWAITING_RESPONSE) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						int retries = 0;
						while (((AckRequestMessage) current.message).getRetryCount() > retries) {
							retries++;
							try {
								Thread.sleep(((AckRequestMessage) current.message).getTimeOutSec() * 1000);
							} catch (InterruptedException e) {
							}
							if (current.state == State.RETRIEVED_ANSWER) {
								// got an answer and that whas handled by
								// inQeue.
								// Just stop watching.
							}
							return;
						}
						// Retries elapsed without any success. Set to TimedOut
						// and reporting error to client
						current.state = State.TIMED_OUT;
						messageListeners.get(current.message.getDispatcherType()).handleResponseMessages(current.state, null,
								current.message);
					}
				}).start();
			}
		}
		outLock.unlock();
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


	/**
	 * @param inMessage
	 * @param listener
	 * @return
	 */
	private MessageEntry getAwaitingRequestMessageForInMessage(Message inMessage, MessageListener listener) {
		outLock.lock();
		for (MessageEntry entry : outQueue) {
			if (entry.state == State.AWAITING_RESPONSE
					&& ((AckRequestMessage) entry.message).getCorrelation().equals(
							((AckResponseMessage) inMessage).getCorrelator()) == true)
				return entry;
		}
		outLock.unlock();
		return null;
	}


	/**
	 * @param inMessage
	 * @param listener
	 * @return
	 */
	private void handleConditionalOutMessageForResponse(Message inMessage) {
		outLock.lock();
		for (MessageEntry entry : outQueue) {
			if (entry.state == State.WAITING_FOR_CONDITION && entry.condition.fullfilled(inMessage)) {
				entry.state = State.PENDING;
			}
		}
		hasOutMessages.signal();
		outLock.unlock();
	}
}
