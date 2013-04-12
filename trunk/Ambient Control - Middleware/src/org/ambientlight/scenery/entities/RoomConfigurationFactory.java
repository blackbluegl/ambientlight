package org.ambientlight.scenery.entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.device.drivers.SwitchDeviceOverEthernetConfiguration;
import org.ambientlight.device.stripe.StripeConfiguration;
import org.ambientlight.device.stripe.StripePartConfiguration;
import org.ambientlight.room.RoomConfiguration;
import org.ambientlight.room.objects.LightObjectConfiguration;
import org.ambientlight.room.objects.RoomItemConfiguration;
import org.ambientlight.room.objects.SwitchObjectConfiguration;
import org.ambientlight.scenery.SceneryConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.rendering.programms.configuration.SunSetRenderingProgrammConfiguration;
import org.ambientlight.scenery.switching.configuration.SwitchingConfiguration;

import com.thoughtworks.xstream.XStream;

public class RoomConfigurationFactory {
	static String DATA_DIRECTORY = System.getProperty("user.home") + File.separator + "ambientlight" + File.separator
			+ "sceneries";

	public static RoomConfiguration getRoomConfigByName(String configuration) throws FileNotFoundException {
		XStream xstream = getXStream();
		FileInputStream input = new FileInputStream(DATA_DIRECTORY + File.separator + configuration + ".xml");
		RoomConfiguration result = (RoomConfiguration) xstream.fromXML(input);

		return result;
	}

	public static void saveRoomConfiguration(RoomConfiguration roomConfiguration, String fileName) throws IOException {
		XStream xstream = getXStream();

		String result = xstream.toXML(roomConfiguration);

		File dir = new File(DATA_DIRECTORY);
		if (dir.exists() == false) {
			dir.mkdirs();
		}

		File file = new File(DATA_DIRECTORY + File.separator + fileName + ".xml");

		FileWriter fw = new FileWriter(file);

		fw.write(result);

		fw.flush();
		fw.close();

		System.out.println("wrote roomConfig to: " + file.getAbsoluteFile());
	}

	private static XStream getXStream() {
		XStream xstream = new XStream();
		xstream.processAnnotations(RoomConfiguration.class);
		xstream.processAnnotations(StripePartConfiguration.class);
		xstream.processAnnotations(LightObjectConfiguration.class);
		xstream.processAnnotations(SwitchObjectConfiguration.class);
		xstream.processAnnotations(RoomItemConfiguration.class);
		xstream.processAnnotations(SceneryConfiguration.class);
		xstream.processAnnotations(StripeConfiguration.class);
		xstream.processAnnotations(DeviceConfiguration.class);
		xstream.processAnnotations(SwitchDeviceOverEthernetConfiguration.class);
		xstream.processAnnotations(SimpleColorRenderingProgramConfiguration.class);
		xstream.processAnnotations(SunSetRenderingProgrammConfiguration.class);
		xstream.processAnnotations(SwitchingConfiguration.class);
		return xstream;
	}
}
