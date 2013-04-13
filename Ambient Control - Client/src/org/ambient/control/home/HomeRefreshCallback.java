package org.ambient.control.home;

import java.util.concurrent.ExecutionException;

public interface HomeRefreshCallback {

	public abstract void refreshRoomContent(String roomServerName) throws InterruptedException, ExecutionException;

}