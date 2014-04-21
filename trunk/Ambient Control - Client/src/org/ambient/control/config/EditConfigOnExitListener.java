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

package org.ambient.control.config;

/**
 * @author Florian Bornkessel
 * 
 */
public interface EditConfigOnExitListener {

	/**
	 * notify listener that the edited object is finished editing and ready to work with. Important! some fragments need the
	 * information. and it is not possible to securely store this value within its lifecycle. So we will give it to the listener
	 * here.
	 * 
	 * @param roomName
	 * 
	 * @param configuration
	 */
	public void onIntegrateConfiguration(String roomName, Object configuration);


	/**
	 * notify listener that the edited object should be reverted. Important! some fragments need the information. and it is not
	 * possible to securely store this value within its lifecycle. So we will give it to the listener here.
	 * 
	 * @param roomName
	 * @param configuration
	 */
	public void onRevertConfiguration(String roomName, Object configuration);
}
