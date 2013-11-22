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

package org.ambientlight.ws.process.validation;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.process.handler.DataTypeValidation;


/**
 * @author Florian Bornkessel
 * 
 */
public class ValidationResult {

	public List<ValidationEntry> invalidateEntries = new ArrayList<ValidationEntry>();


	public void addForkWithoutCorrespondingHandler(int nodeId) {
		ForkHasNoCorrespondingHandler entry = new ForkHasNoCorrespondingHandler();
		entry.nodeId = nodeId;
		invalidateEntries.add(entry);
	}

	public void addEmptyActionHandlerEntry(int nodeId) {
		EmptyActionHandlerEntry entry = new EmptyActionHandlerEntry();
		entry.nodeId = nodeId;
		invalidateEntries.add(entry);
	}


	public void addEntry(int nodeId, int previousNodeId, DataTypeValidation previousNodeGenerates,
			DataTypeValidation[] nodeConsumes) {
		InvalidFormatEntry entry = new InvalidFormatEntry();
		entry.nodeId = nodeId;
		entry.previousNodeId = previousNodeId;
		entry.previousNodeGenerates = previousNodeGenerates;
		entry.nodeConsumes = nodeConsumes;
		invalidateEntries.add(entry);
	}


	public boolean resultIsValid() {
		if (invalidateEntries.size() > 0)
			return false;
		return true;
	}

}
