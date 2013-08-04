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

package org.ambient.control.processes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * @author Florian Bornkessel
 * 
 */
public class ConfigBindingHelper {

	public static List<String> getArrayList(Object sourceBean, String path) {
		String[] pathElements = path.split("\\.");
		Object result = null;
		try {
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
		if (positionInPath < path.length - 1)
			return getObjectRecursively(result, path, positionInPath + 1);
		else
			return result;
	}
}
