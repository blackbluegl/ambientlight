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

import android.util.Log;


/**
 * @author Florian Bornkessel
 * 
 */
public class ValueBindingHelper {

	private static final String LOG = "ConfigBindingHelper";


	public static org.ambient.control.config.AlternativeValues getValuesForField(Value[] valuesAnnotation, Object bean,
			Object dataModell) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		org.ambient.control.config.AlternativeValues result = new org.ambient.control.config.AlternativeValues();

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

			if (currentValueAnnotation.forSubClass().isEmpty()
					|| currentValueAnnotation.forSubClass().equals(bean.getClass().getName())) {
				Log.d(LOG, "value matches for class: " + bean.getClass().getName());
			} else {
				continue;
			}

			// hardcoded case
			if (currentValueAnnotation.value().isEmpty() == false) {
				result.values.add(currentValueAnnotation.value());
				result.displayValues.add(currentValueAnnotation.displayValue() != null ? currentValueAnnotation.displayValue()
						: "");
			}

			// value provider
			if (currentValueAnnotation.valueProvider().isEmpty() == false) {
				Class<?> valueProvider = Class.forName(currentValueAnnotation.valueProvider());
				AlternativeValueProvider provider = (AlternativeValueProvider) valueProvider.newInstance();
				org.ambient.control.config.AlternativeValues providerResult = provider.getValue(dataModell, bean);
				result.displayValues.addAll(providerResult.displayValues);
				result.values.addAll(providerResult.values);
			}

		}
		return result;
	}


	public static org.ambient.control.config.AlternativeValues getValuesForClass(AlternativeClassValues annotation)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		org.ambient.control.config.AlternativeValues result = new org.ambient.control.config.AlternativeValues();

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

			if (currentValueAnnotation.value().isEmpty() == false) {
				result.values.add(currentValueAnnotation.value());
				result.displayValues.add(currentValueAnnotation.displayValue() != null ? currentValueAnnotation.displayValue()
						: "");
			}
		}
		return result;
	}


	public static CharSequence[] toCharSequenceArray(List<String> input) {
		CharSequence[] result = new CharSequence[input.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = input.get(i);
		}
		return result;
	}

}
