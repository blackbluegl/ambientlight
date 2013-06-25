package org.ambient.control.home;


public interface HomeRefreshCallback {

	public abstract void refreshRoomContent(String roomServerName) throws Exception;

}