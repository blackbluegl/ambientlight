package org.ambientlight.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Presentation {

	public int groupPosition() default 0;


	public int position() default 0;
	public String name() default "";

	public String description() default "";
}
