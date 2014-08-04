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

package org.ambientlight.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * @author Florian Bornkessel
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

	/*
	 * used if childclasses should have seperate implementations for this annotation
	 */
	public Class<?> forSubClass() default Object.class;


	/*
	 * give hint for an abstract class what kind of concrete class type can be used for new instances.
	 */
	public Class<?> newClassInstanceType() default Object.class;


	/*
	 * display name for new class instance.
	 */
	public String displayNewClassInstance() default "";


	/*
	 * provides keys for maps and values for maps or list.
	 */
	public Class<?> valueProvider() default Object.class;
}
