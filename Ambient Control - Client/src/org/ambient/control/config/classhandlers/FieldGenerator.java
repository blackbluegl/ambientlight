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

import org.ambient.control.config.ConfigBindingHelper;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.ws.Room;


/**
 * @author Florian Bornkessel
 * 
 */
public abstract class FieldGenerator {

	// a lot of fields have alternative values - spinners, lists and so on.
	// they are bound in several ways
	protected List<String> altValues = null;
	// a mapping for the alternative values - (user friendly)
	protected List<String> altValuesToDisplay = null;

	protected Room roomConfig = null;
	protected Object config = null;
	protected Field field = null;


	public FieldGenerator(Room roomConfig, Object config, Field field) throws IllegalAccessException, ClassNotFoundException,
	InstantiationException {
		super();
		this.roomConfig = roomConfig;
		this.config = config;
		this.field = field;

		createAltValues();
	}


	protected void createAltValues() throws IllegalAccessException, ClassNotFoundException, java.lang.InstantiationException {

		if (field.getAnnotation(AlternativeValues.class) != null) {
			// get the binding information from the field annotation first
			altValues = ConfigBindingHelper.getAlternativeValues(field.getAnnotation(AlternativeValues.class), config.getClass()
					.getName(), roomConfig, config);
			altValuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(field.getAnnotation(AlternativeValues.class),
					config.getClass().getName(), roomConfig, config);
		} else if (field.getDeclaringClass().getAnnotation(AlternativeValues.class) != null) {
			// if there is no information in the field, descend to the class
			// that
			// is held by the field and get the annotation from the class.
			// Useful if a class is a subclass and needs special value binding
			altValues = ConfigBindingHelper.getAlternativeValues(
					field.getDeclaringClass().getAnnotation(AlternativeValues.class), config.getClass().getName(), roomConfig,
					config);
			altValuesToDisplay = ConfigBindingHelper.getAlternativeValuesForDisplay(
					field.getDeclaringClass().getAnnotation(AlternativeValues.class), config.getClass().getName(), roomConfig,
					config);
		}
	}

}
