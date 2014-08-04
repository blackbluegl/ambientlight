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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ambientlight.annotations.AlternativeClassValues;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.ws.Room;

import android.widget.LinearLayout;


/**
 * Visual representation of fields. Sets the environment and handles the annotation to alternative value binding here. E.g. Lists
 * for new classInstances, lists with alternative values and so on.
 * 
 * @author Florian Bornkessel
 * 
 */
public abstract class FieldGenerator {

	protected List<Object> altValues = new ArrayList<Object>();

	protected List<String> altValuesToDisplay = new ArrayList<String>();

	protected List<String> altClassInstanceValues = new ArrayList<String>();

	protected List<String> altClassInstancesToDisplay = new ArrayList<String>();

	protected List<Object> altKeys = new ArrayList<Object>();

	protected List<String> altKeysToDisplay = new ArrayList<String>();

	protected Room roomConfig = null;
	protected Object bean = null;
	protected Field field = null;
	protected EditConfigFragment contextFragment;
	protected LinearLayout contentArea;


	public FieldGenerator(Room roomConfig, Object bean, Field field, EditConfigFragment contextFragment, LinearLayout contentArea)
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
		org.ambientlight.annotations.valueprovider.api.AlternativeValues result = new org.ambientlight.annotations.valueprovider.api.AlternativeValues();

		// try to find annotation from field
		AlternativeValues annotation = field.getAnnotation(AlternativeValues.class);
		if (annotation != null) {

			// bind result for child classes to handle
			result = ValueBindingHelper.getValuesForField(annotation.values(), bean, roomConfig);

			altValues = result.values;
			altValuesToDisplay = result.displayValues;
			altClassInstanceValues = result.classNames;
			altClassInstancesToDisplay = result.displayClassNames;
			altKeys = result.keys;
			altKeysToDisplay = result.displayKeys;
		}

		// if no annotation or no classInstanceValues where found from the field annotation, try to find it from the declaring
		// class
		if (result.classNames.isEmpty()) {
			AlternativeClassValues classAnnotation = null;

			// try to get the value annotation from a map if object is a map
			Object fieldObject = field.get(bean);
			if (fieldObject != null && fieldObject instanceof Map) {
				ParameterizedType pt = (ParameterizedType) field.getGenericType();
				Class<?> clazz = (Class<?>) pt.getActualTypeArguments()[1];

				classAnnotation = clazz.getAnnotation(AlternativeClassValues.class);
			}
			// object is not a map. get it directly from the class
			else {
				classAnnotation = field.getType().getAnnotation(AlternativeClassValues.class);
			}

			// if no classAnnotation are found do not continue
			if (classAnnotation == null)
				return;

			// bind result for child classes to handle
			result = ValueBindingHelper.getValuesForClass(classAnnotation);
			altClassInstanceValues = result.classNames;
			altClassInstancesToDisplay = result.displayClassNames;
		}
	}


	/**
	 * Get sure that you use this method in the ListenerCallback Definition of your subclasses to gain access to the values
	 * reference. If you create a local variable in the callbacks and modify the reference it does not have an effect to the beans
	 * field.
	 * 
	 * @return
	 */
	protected Serializable getFieldValue() {

		try {
			return (Serializable) field.get(bean);
		} catch (Exception e1) {

			e1.printStackTrace();
			return null;
		}
	}
}
