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
			Log.d(LOG, "dataModel is empty!");
			return result;
		}

		for (Value currentValueAnnotation : valuesAnnotation) {

			// either the annotation is bound to a field of a concrete class or its bound to a parent class and there are
			// different
			// annotations for each subclass given.
			if (currentValueAnnotation.forSubClass().isEmpty()
					|| currentValueAnnotation.forSubClass().equals(bean.getClass().getName())) {
				Log.d(LOG, "value matches for class: " + bean.getClass().getName());
			} else {
				continue;
			}

			// hardcoded case for new classes
			if (currentValueAnnotation.newClassInstanceType().isEmpty() == false) {
				result.classNames.add(currentValueAnnotation.newClassInstanceType());
				// set display value - if no display value given use class name
				result.displayValues.add(currentValueAnnotation.displayValue().isEmpty() == false ? currentValueAnnotation
						.displayValue() : currentValueAnnotation.newClassInstanceType());
			}

			// value provider for beans that already exist
			if (currentValueAnnotation.valueProvider().isEmpty() == false) {
				AlternativeValueProvider provider = (AlternativeValueProvider) Class.forName(
						currentValueAnnotation.valueProvider()).newInstance();
				AlternativeValues providerResult = provider.getValue(dataModell, bean);
				result.displayValues.addAll(providerResult.displayValues);
				result.values.addAll(providerResult.values);

				// if provider did not set the display values add toString from Values
				if (result.displayValues == null || result.displayValues.isEmpty()) {
					for (Object current : result.values) {
						result.displayValues.add(current.toString());
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
	public static org.ambientlight.annotations.valueprovider.api.AlternativeClassValues getValuesForClass(
			AlternativeClassValues annotation) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		org.ambientlight.annotations.valueprovider.api.AlternativeClassValues result = new org.ambientlight.annotations.valueprovider.api.AlternativeClassValues();

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
			if (currentValueAnnotation.newClassInstanceType().isEmpty() == false) {
				result.classNames.add(currentValueAnnotation.newClassInstanceType());
				// add display values, if not present add classNames
				result.displayValues.add(currentValueAnnotation.displayValue() != null ? currentValueAnnotation.displayValue()
						: currentValueAnnotation.newClassInstanceType());
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
