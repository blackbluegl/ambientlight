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

package org.ambientlight.config.process.handler.actor;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.process.handler.AbstractActionHandlerConfiguration;
import org.ambientlight.config.process.handler.DataTypeValidation;
import org.ambientlight.room.entities.features.EntityId;
import org.ambientlight.ws.process.validation.HandlerDataTypeValidation;


/**
 * @author Florian Bornkessel
 * 
 */
@HandlerDataTypeValidation(consumes = { DataTypeValidation.CONSUMES_NO_DATA, DataTypeValidation.BOOLEAN }, generates = DataTypeValidation.CREATES_NO_DATA)
public class SwitchableHandlerConfiguration extends AbstractActionHandlerConfiguration {

	private static final long serialVersionUID = 1L;

	public List<EntityId> switcheables = new ArrayList<EntityId>();

	public boolean powerState;

	public boolean useTokenValue;

	public boolean fireEvent;

	public boolean invert;
}