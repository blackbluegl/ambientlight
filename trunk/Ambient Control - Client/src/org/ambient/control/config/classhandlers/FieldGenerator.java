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

package org.ambient.control.config.classhandlers;

import java.lang.reflect.Field;
import java.util.List;

import org.ambient.control.config.EditConfigHandlerFragment;
import org.ambient.control.config.ValueBindingHelper;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.ws.Room;

import android.widget.LinearLayout;


/**
 * @author Florian Bornkessel
 * 
 */
public abstract class FieldGenerator {

	protected List<Object> altValues = null;

	protected List<String> altValuesToDisplay = null;

	protected Room roomConfig = null;
	protected Object bean = null;
	protected Field field = null;
	protected EditConfigHandlerFragment contextFragment;
	protected LinearLayout contentArea;


	public FieldGenerator(Room roomConfig, Object bean, Field field, EditConfigHandlerFragment contextFragment, LinearLayout contentArea)
			throws IllegalAccessException, ClassNotFoundException, InstantiationException {
		super();
		this.roomConfig = roomConfig;
		this.bean = bean;
		this.field = field;
		this.contextFragment = contextFragment;
		this.contentArea = contentArea;

		createAltValues();
	}


	protected void createAltValues() throws IllegalAccessException, ClassNotFoundException, java.lang.InstantiationException {

		// try to find annotation from field
		AlternativeValues annotation = field.getAnnotation(AlternativeValues.class);

		// if no annotation is found in this concrete class try to find it from parent class
		if (annotation == null) {
			annotation = field.getDeclaringClass().getAnnotation(AlternativeValues.class);
		}

		org.ambient.control.config.AlternativeValues result = ValueBindingHelper.getValuesForField(annotation.values(), bean,
				roomConfig);

		altValues = result.values;
		altValuesToDisplay = result.displayValues;
	}

}
