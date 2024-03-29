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

package org.ambientlight.config.room.entities.remoteswitches;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ambientlight.config.device.drivers.RemoteSwitchBridgeConfiguration;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitch;
import org.ambientlight.ws.EntityIdDeserializer;
import org.ambientlight.ws.EntityIdSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * @author Florian Bornkessel
 * 
 */
public class RemoteSwitchManagerConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	public RemoteSwitchBridgeConfiguration device;

	@JsonSerialize(keyUsing = EntityIdSerializer.class)
	@JsonDeserialize(keyUsing = EntityIdDeserializer.class)
	public Map<EntityId, RemoteSwitch> remoteSwitches = new HashMap<EntityId, RemoteSwitch>();
}
