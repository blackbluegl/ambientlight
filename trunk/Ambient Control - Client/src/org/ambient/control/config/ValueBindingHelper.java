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

import java.util.List;

import org.ambientlight.annotations.AlternativeClassValues;
import org.ambientlight.annotations.ClassValue;
import org.ambientlight.annotations.Value;
import org.ambientlight.annotations.valueprovider.api.AlternativeValueProvider;
import org.ambientlight.annotations.valueprovider.api.AlternativeValues;
import org.ambientlight.ws.Room;

import android.util.Log;


/**
 * @author Florian Bornkessel
 * 
 */
public class ValueBindingHelper {

	private static final String LOG = "ConfigBindingHelper";


	/**
	 * resolve "Value" annotation and generate alternative values as objects and user friendly to display. Either hardcoded values
	 * can be given or value provider may generate the list. In the case that the field is bound to a parent class the annotation
	 * may contain a "forSubClass" property that defines for which concrete class the annotation is valid. If no display value is
	 * given a fallback to the object value is used. You may implement a Valueprovider for more complex cases.
	 * 
	 * @param valuesAnnotation
	 * @param bean
	 * @param dataModell
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static AlternativeValues getValuesForField(Value[] valuesAnnotation, Object bean, Room dataModell)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		AlternativeValues result = new AlternativeValues();

		if (valuesAnnotation == null || valuesAnnotation.length == 0) {
			Log.d(LOG, "annotation is empty!");
			return result;
		}

		if (bean == null) {
			Log.d(LOG, "bean is empty!");
			return result;
		}

		if (dataModell == null) {
			Log.d(LOG, "Room is null!");
			return result;
		}

		for (Value currentValueAnnotation : valuesAnnotation) {

			// either the annotation is valid for the concrete class and all children or its bound to a parent class and there are
			// different annotations for each subclass given.
			if (currentValueAnnotation.forSubClass().getName().equals(Object.class.getName())
					|| currentValueAnnotation.forSubClass().getName().equals(bean.getClass().getName())) {
				Log.d(LOG, "value matches for class: " + bean.getClass().getName());
			} else {
				// does not match for this calls and will be ignored
				continue;
			}

			// hard coded case for new classes
			if (currentValueAnnotation.newClassInstanceType().getName().equals(Object.class.getName()) == false) {
				result.classNames.add(currentValueAnnotation.newClassInstanceType().getName());
				// set display value - if no display value given use class name
				result.displayClassNames
				.add(currentValueAnnotation.displayNewClassInstance().isEmpty() == false ? currentValueAnnotation
						.displayNewClassInstance() : currentValueAnnotation.newClassInstanceType().getName());
			}

			// value provider for keys, values and new class instances
			if (currentValueAnnotation.valueProvider().getName().equals(Object.class.getName()) == false) {
				// get generated values from provider
				AlternativeValueProvider provider = (AlternativeValueProvider) currentValueAnnotation.valueProvider()
						.newInstance();
				AlternativeValues providerResult = provider.getValue(dataModell, bean);

				// if the provider did not generate any usefull result continue with next annotation
				if (providerResult == null || providerResult.values == null) {
					continue;
				}

				// add values
				result.values.addAll(providerResult.values);
				result.displayValues.addAll(providerResult.displayValues);
				// if provider did not set the display values add via toString() from Values
				if (result.displayValues == null || result.displayValues.isEmpty()) {
					for (Object current : result.values) {
						result.displayValues.add(current.toString());
					}
				}

				// add new class instances
				result.classNames.addAll(providerResult.classNames);
				result.displayClassNames.addAll(providerResult.displayClassNames);
				// if provider did not set the display values add via toString() from Values
				if (result.displayClassNames == null || result.displayClassNames.isEmpty()) {
					for (String current : result.classNames) {
						result.displayClassNames.add(current);
					}
				}

				// add keys
				result.keys.addAll(providerResult.keys);
				result.displayKeys.addAll(providerResult.displayKeys);
				// if provider did not set the display values add via toString() from Values
				if (result.displayKeys == null || result.displayKeys.isEmpty()) {
					for (Object current : result.keys) {
						result.displayKeys.add(current.toString());
					}
				}
			}
		}
		return result;
	}


	/**
	 * Classes may be annotated with alternative values. These annotations are used when a new concrete bean shall be initialized.
	 * The alternatives define a className and a display name.
	 * 
	 * @param annotation
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static org.ambientlight.annotations.valueprovider.api.AlternativeValues getValuesForClass(
			AlternativeClassValues annotation) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		org.ambientlight.annotations.valueprovider.api.AlternativeValues result = new org.ambientlight.annotations.valueprovider.api.AlternativeValues();

		if (annotation == null) {
			Log.d(LOG, "annotation is empty!");
			return result;
		}

		ClassValue[] valuesAnnotation = annotation.values();

		if (valuesAnnotation == null || valuesAnnotation.length == 0) {
			Log.d(LOG, "annotation exists but no class values are defined!");
			return result;
		}

		for (ClassValue currentValueAnnotation : valuesAnnotation) {
			if (currentValueAnnotation.newClassInstanceType().getName().equals(Object.class.getName()) == false) {
				result.classNames.add(currentValueAnnotation.newClassInstanceType().getName());
				// add display values, if not present add classNames
				result.displayClassNames.add(currentValueAnnotation.displayValue() != null ? currentValueAnnotation
						.displayValue()
						: currentValueAnnotation.newClassInstanceType().getName());
			}
		}

		return result;
	}


	/**
	 * helper method to convert Lists of String to Charsequence Array
	 * 
	 * @param input
	 * @return
	 */
	public static CharSequence[] toCharSequenceArray(List<String> input) {
		CharSequence[] result = new CharSequence[input.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = input.get(i);
		}
		return result;
	}

}
