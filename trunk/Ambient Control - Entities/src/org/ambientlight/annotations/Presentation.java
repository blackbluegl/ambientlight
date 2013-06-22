package org.ambientlight.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Presentation {
	
	public String  position() default "0";
	public String name() default "";
}
