package org.ambientlight.device.drivers;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("device")

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class DeviceConfiguration {
}
