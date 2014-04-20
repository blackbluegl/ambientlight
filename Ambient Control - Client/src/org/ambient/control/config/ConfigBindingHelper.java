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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.ambientlight.annotations.AlternativeIds;
import org.ambientlight.annotations.AlternativeValues;
import org.ambientlight.annotations.Value;
import org.ambientlight.annotations.ValueBindingPath;

import android.util.Log;


/**
 * @author Florian Bornkessel
 * 
 */
public class ConfigBindingHelper {

	private static final String LOG = "ConfigBindingHelper";


	public static List<String> getAlternativeIds(AlternativeIds annotation, Object sourceBean) {

		String path = annotation.idBinding();
		return getByPathBinding(sourceBean, path);

	}


	public static List<String> getAlternativeValues(AlternativeValues annotation, String className, Object sourceBean,
			Object entity) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Log.d(LOG, "finding binding for field with type: " + className + " in class: " + sourceBean.getClass().getName());
		ValueBindingPath[] valueBinding = annotation.valueBinding();
		if (valueBinding.length != 0) {
			for (ValueBindingPath current : valueBinding) {
				if (current.forSubClass().isEmpty() || current.forSubClass().equals(className))
					return getByPathBinding(sourceBean, current.valueBinding());
			}

		} else if (annotation.values().length > 0) {
			List<String> result = new ArrayList<String>();
			for (Value current : annotation.values()) {
				if (current.forSubClass().isEmpty() || current.forSubClass().equals(className)) {
					result.add(current.value());
				}
			}
			return result;
		} else if (annotation.valueProvider().isEmpty() == false) {
			Class valueProvider = Class.forName(annotation.valueProvider());
			AlterativeValueProvider provider = (AlterativeValueProvider) valueProvider.newInstance();
			return provider.getValue(sourceBean, entity);
		}

		return null;
	}


	public static List<String> getAlternativeValuesForDisplay(AlternativeValues annotation, String className, Object sourceBean,
			Object entity) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		List<String> result = new ArrayList<String>();
		for (Value current : annotation.values()) {
			result.add(current.displayName());
		}

		if (result.size() == 0)
			return getAlternativeValues(annotation, className, sourceBean, entity);

		return result;
	}


	public static CharSequence[] toCharSequenceArray(List<String> input) {
		CharSequence[] result = new CharSequence[input.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = input.get(i);
		}
		return result;
	}


	@SuppressWarnings("unchecked")
	private static List<String> getByPathBinding(Object sourceBean, String path) {
		String[] pathElements = path.split("\\.");
		Object result = null;
		try {
			Log.d(LOG, "binding path: " + path + " for bean: " + sourceBean.getClass().getName());
			result = getObjectRecursively(sourceBean, pathElements, 0);
		} catch (Exception e) {
			Log.e(LOG, "error binding path: " + path, e);
		}

		if (result instanceof List)
			return (List<String>) result;
		else if (result instanceof Set)
			return new ArrayList<String>((Set<String>) result);
		else if (result instanceof String[])
			return Arrays.asList((String[]) result);
		else if (result instanceof String) {
			ArrayList<String> resultArray = new ArrayList<String>();
			resultArray.add((String) result);
			return resultArray;
		} else
			return null;
	}


	private static Object getObjectRecursively(Object current, String[] path, int positionInPath) throws NoSuchMethodException,
	IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Object result = null;
		Class<?> currentClass = current.getClass();
		if (path[positionInPath].endsWith(")")) {
			Method methodToCall = currentClass.getMethod(path[positionInPath].substring(0, path[positionInPath].length() - 2),
					(Class<?>[]) null);
			result = methodToCall.invoke(current, (Object[]) null);
		} else {
			Field fieldForResult = currentClass.getField(path[positionInPath]);
			result = fieldForResult.get(current);
		}

		if (positionInPath < path.length - 1) {
			if (result instanceof Iterable) {
				ArrayList<Object> arrayToReturn = new ArrayList<Object>();
				for (Object currentToDescend : (Iterable<?>) result) {
					arrayToReturn.add(getObjectRecursively(currentToDescend, path, positionInPath + 1));
				}
				return arrayToReturn;
			} else
				return getObjectRecursively(result, path, positionInPath + 1);
		} else
			return result;
	}
}
