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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;


/**
 * @author Florian Bornkessel
 * 
 */
public class Bootstrap extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private List<String> roomNames = new ArrayList<String>();


	@Override
	public void init() throws ServletException {
		super.init();

		File configDir = new File(Persistence.DATA_DIRECTORY);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
		String[] filenames = configDir.list(filter);
		for (String currentFileName : filenames) {
			try {
				roomNames.add(AmbientControlMW.initRoom(currentFileName));
			} catch (Exception e) {
				throw new ServletException(e);
			}
		}
	}


	@Override
	public void destroy() {
		super.destroy();
		for (String currentRoom : roomNames) {
			AmbientControlMW.destroyRoom(currentRoom);
		}
	}
}
