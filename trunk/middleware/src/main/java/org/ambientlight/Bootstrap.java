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

package org.ambientlight;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.ambientlight.device.drivers.DeviceDriverFactory;
import org.ambientlight.room.RoomFactory;


/**
 * @author Florian Bornkessel
 * 
 */
public class Bootstrap extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private RoomFactory roomFactory;


	@Override
	public void init() throws ServletException {
		super.init();

		roomFactory = new RoomFactory(new DeviceDriverFactory());

		AmbientControlMW.roomConfigFileName = "wohnzimmer";
		AmbientControlMW.debug = true;
		try {
			AmbientControlMW.init();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}


	@Override
	public void destroy() {
		super.destroy();
		AmbientControlMW.getRoomFactory().destroyRoom();
		AmbientControlMW.room = null;
	}
}
