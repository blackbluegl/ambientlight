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


/**
 * @author Florian Bornkessel
 * 
 */
public class ConfigBindingHelper {

	public static List<String> getAlternativeIds(AlternativeIds annotation, Object sourceBean) {

		String path = annotation.idBinding();
		return getByPathBinding(sourceBean, path);

	}


	public static List<String> getAlternativeValues(AlternativeValues annotation, String className, Object sourceBean) {

		ValueBindingPath[] pathes = annotation.valueBinding();
		if (pathes.length != 0) {
			for (ValueBindingPath current : pathes) {
				if (current.forSubClass().isEmpty() || current.forSubClass().equals(className))
					return getByPathBinding(sourceBean, current.valueBinding());
			}
		} else {
			List<String> result = new ArrayList<String>();
			for (Value current : annotation.values()) {
				if (current.forSubClass().isEmpty() || current.forSubClass().equals(className)) {
					result.add(current.value());
				}
			}
			return result;
		}
		return null;
	}


	public static List<String> getAlternativeValuesForDisplay(AlternativeValues annotation, String className, Object sourceBean) {

		List<String> result = new ArrayList<String>();
		for (Value current : annotation.values()) {
			result.add(current.displayName());
		}

		if (result.size() == 0)
			return getAlternativeValues(annotation, className, sourceBean);

		return result;
	}


	public static CharSequence[] toCharSequenceArray(List<String> input) {
		CharSequence[] result = new CharSequence[input.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = input.get(i);
		}
		return result;
	}


	/**
	 * @param sourceBean
	 * @param path
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<String> getByPathBinding(Object sourceBean, String path) {
		String[] pathElements = path.split("\\.");
		Object result = null;
		try {
			System.out.println("binding path: " + path + " for bean: " + sourceBean.getClass().getName());
			result = getObjectRecursively(sourceBean, pathElements, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (result instanceof List)
			return (List<String>) result;
		else if (result instanceof Set)
			return new ArrayList((Set) result);
		else if (result instanceof String[])
			return Arrays.asList((String[]) result);
		else if (result instanceof String) {
			ArrayList<String> resultArray = new ArrayList<String>();
			resultArray.add((String) result);
			return resultArray;
		}
		else
			return null;
	}


	private static Object getObjectRecursively(Object current, String[] path, int positionInPath) throws NoSuchMethodException,
	IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Object result = null;
		Class currentClass = current.getClass();
		if (path[positionInPath].endsWith(")")) {
			Method methodToCall = currentClass.getMethod(path[positionInPath].substring(0, path[positionInPath].length() - 2),
					null);
			result = methodToCall.invoke(current, null);
		} else {
			Field fieldForResult = currentClass.getField(path[positionInPath]);
			result = fieldForResult.get(current);
		}

		if (positionInPath < path.length - 1) {
			if (result instanceof Iterable) {
				ArrayList arrayToReturn = new ArrayList();
				for (Object currentToDescend : (Iterable) result) {
					arrayToReturn.add(getObjectRecursively(currentToDescend, path, positionInPath + 1));
				}
				return arrayToReturn;
			} else
				return getObjectRecursively(result, path, positionInPath + 1);
		} else
			return result;
	}
}
