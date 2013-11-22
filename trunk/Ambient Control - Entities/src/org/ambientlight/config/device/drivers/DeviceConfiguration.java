package org.ambientlight.config.device.drivers;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("device")

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class DeviceConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;
}
