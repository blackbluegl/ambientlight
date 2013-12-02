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

	private boolean retryOngoin = false;

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
					try {
						outLock.lock();
						hasOutMessages.await();
						boolean allHandled = handleOutMessages();
						outLock.unlock();
						if (!allHandled) {
							retryHandleOutMessages();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {

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
		// outLock.lock();
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

		this.startWatchDogForWaitingMessages();

		// if an error while sending occoured we have to retry sending again
		if (sendFailureOccured)
			return false;
		else
			return true;
	}


	private void handleInMessages() {
		for (final Message inMessage : inQeue) {
			final MessageListener listener = messageListeners.get(inMessage.getDispatcherType());
			if (listener == null) {
				continue;
			}

			handleConditionalOutMessageForResponse(inMessage);

			if (inMessage instanceof AckResponseMessage) {
				// there maybe an outMessage waiting for
				// this message
				final MessageEntry foundRequest = getAwaitingRequestMessageForInMessage(inMessage, listener);
				if (foundRequest != null) {
					foundRequest.state = State.RETRIEVED_ANSWER;
					new Thread(new Runnable() {

						@Override
						public void run() {
							listener.handleResponseMessages(foundRequest.state, inMessage, foundRequest.message);
						}
					}).start();
				} else {
					System.out
					.println("QeueManager handleInMessages: got an AckResponseMessage where no Request is listening to (anymore): "
							+ inMessage);
				}
			} else {
				new Thread(new Runnable() {

					@Override
					public void run() {
						listener.handleMessage(inMessage);
					}
				}).start();
			}
		}
		inQeue.clear();
	}


	private void startWatchDogForWaitingMessages() {
		// outLock.lock();
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
		// outLock.unlock();
	}


	private void retryHandleOutMessages() {
		if (retryOngoin == true)
			return;
		System.out.println("QeueuManager - retryHandleOutMessages(): some Messages could not be sent. Retrying!");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					System.out.println("QeueuManager - retryHandleOutMessages(): Retrying to send waiting OutMessages");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				outLock.lock();
				retryOngoin = false;
				hasOutMessages.signal();
				outLock.unlock();
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
		MessageEntry entry = null;
		for (MessageEntry current : outQueue) {
			if (current.state == State.AWAITING_RESPONSE
					&& ((AckRequestMessage) current.message).getCorrelation().equals(
							((AckResponseMessage) inMessage).getCorrelator()) == true) {
			}
			entry = current;
			break;
		}
		outLock.unlock();
		return entry;
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
