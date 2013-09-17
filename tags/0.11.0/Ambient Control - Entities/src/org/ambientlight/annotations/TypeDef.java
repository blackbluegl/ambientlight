package org.ambientlight.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TypeDef {
	 
	    public String min() default "0";
	 
	    public String max() default "0";
	 
	    public FieldType fieldType() default FieldType.NUMERIC;
}
