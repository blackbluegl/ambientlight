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
		PENDING, SENDING, SENT, WAITING_FOR_CONDITION, WAITING_FOR_RESPONSE, SEND_ERROR, TIMED_OUT, RETRIEVED_ANSWER;
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
		List<MessageEntry> removeAfterSent = new ArrayList<MessageEntry>();
		for (MessageEntry out : outQueue) {

			// only send ready messages
			if (out.state != State.PENDING) {
				continue;
			}

			out.state = State.SENDING;
			if (dispatcherManager.dispatchMessage(outDispatchers.get(out.message.getDispatcherType()), out.message)) {

				if (out.message instanceof AckRequestMessage) {
					out.state = State.WAITING_FOR_RESPONSE;
					this.startWatchDogForWaitingMessage(out);
				} else {
					out.state = State.SENT;
					removeAfterSent.add(out);
				}
			} else {
				out.state = State.SEND_ERROR;
				sendFailureOccured = true;
			}

		}
		outQueue.removeAll(removeAfterSent);

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
				System.out.println("QueueManager - handleInMessages(): got no listener for MessageDispatcherType: "
						+ inMessage.getDispatcherType());
				continue;
			}

			// set State to pending if condition is fullfilled for some
			// outmessages
			handleConditionalOutMessageForResponse(inMessage);

			// there maybe an outMessage waiting for
			// this response
			boolean messageHandled = false;
			if (inMessage instanceof AckResponseMessage) {

				final MessageEntry foundRequest = getAwaitingRequestMessageForInMessage(inMessage, listener);
				// there is a request waiting
				if (foundRequest != null) {
					foundRequest.state = State.RETRIEVED_ANSWER;
					// decouple listeners action to handle next message
					new Thread(new Runnable() {

						@Override
						public void run() {
							listener.handleResponseMessages(foundRequest.state, inMessage, foundRequest.message);
						}
					}).start();
					messageHandled = true;
				}
				// maybe it timed out or ther was never a request for this
				// message. Log and handle it the
				else {
					System.out
					.println("QeueManager handleInMessages: got an AckResponseMessage where no correlation could be found for: "
							+ inMessage);
					messageHandled = false;
				}
			}

			// call listener
			if (!messageHandled) {
				// decouple listeners action to handle next message
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


	private void startWatchDogForWaitingMessage(final MessageEntry current) {
		current.state = State.WAITING_FOR_RESPONSE;

		new Thread(new Runnable() {

			@Override
			public void run() {
				int retries = 0;

				while (((AckRequestMessage) current.message).getRetryCount() > retries) {
					// wait
					retries++;
					try {
						Thread.sleep(((AckRequestMessage) current.message).getTimeOutSec() * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (current.state == State.RETRIEVED_ANSWER) {
						// got an answer - just stop watching.
						break;
					} else {
						outLock.lock();
						// sending the message again!
						System.out
								.println("QeueManager - WhatchDogForWaitingMessages(): requestMessage got no answer. Retrying. "
										+ (((AckRequestMessage) current.message).getRetryCount() - retries)
										+ " retries left for Message: " + current.message);

						boolean delivered = dispatcherManager.dispatchMessage(
								outDispatchers.get(current.message.getDispatcherType()), current.message);
						// the dispatcher system was not ready. so this retry
						// does not count
						if (delivered == false) {
							retries--;
						}
						outLock.unlock();
					}
				}

				// finished waiting - remove the message from queue
				outLock.lock();
				outQueue.remove(current);
				outLock.unlock();

				// if we got no answer we have to inform the waiting client
				if (current.state == State.WAITING_FOR_RESPONSE) {
					System.out
					.println("QeueManager - WhatchDogForWaitingMessages(): requestMessage got no answer and timed out. Informing client: "
							+ current.message);
					current.state = State.TIMED_OUT;
					messageListeners.get(current.message.getDispatcherType()).handleResponseMessages(current.state, null,
							current.message);
				}
			}
		}).start();
	}


	private void retryHandleOutMessages() {
		if (retryOngoin == true)
			return;

		System.out.println("QeueuManager - retryHandleOutMessages(): some Messages could not be sent. Retrying!");
		retryOngoin = true;

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
				// reset - this method can be triggered again afterwards
				retryOngoin = false;
				// maybe another process triggered a delivery cycle and we do
				// not need to take care of stalled messages
				if (outQueue.isEmpty() == false) {
					hasOutMessages.signal();
				}
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
		MessageEntry result = null;
		for (MessageEntry current : outQueue) {
			if (current.state == State.WAITING_FOR_RESPONSE
					&& ((AckRequestMessage) current.message).getCorrelation().equals(
							((AckResponseMessage) inMessage).getCorrelator()) == true) {
			}
			result = current;
			break;
		}
		outLock.unlock();
		return result;
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
				hasOutMessages.signal();
				System.out
				.println("QeueManager - handleConditionalOutMessageForResponse(): condition fullfilled. Message may be send now: "
						+ entry.toString());
			}
		}
		outLock.unlock();
	}
}
