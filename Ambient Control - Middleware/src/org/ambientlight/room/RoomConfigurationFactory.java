package org.ambientlight.room;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.device.drivers.SwitchDeviceOverEthernetConfiguration;
import org.ambientlight.device.led.StripeConfiguration;
import org.ambientlight.device.led.StripePartConfiguration;
import org.ambientlight.room.actors.ActorConfiguration;
import org.ambientlight.room.actors.LightObjectConfiguration;
import org.ambientlight.room.actors.SwitchObjectConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.scenery.actor.renderingprogram.TronRenderingProgrammConfiguration;

import com.thoughtworks.xstream.XStream;


public class RoomConfigurationFactory {

	private static ReentrantLock saveLock = new ReentrantLock();

	static String DATA_DIRECTORY = System.getProperty("user.home") + File.separator + "ambientlight" + File.separator
			+ "sceneries";


	public static void beginTransaction() {
		saveLock.lock();
	}


	public static void commitTransaction() throws IOException {
		try {
			saveRoomConfiguration(AmbientControlMW.getRoom().config, AmbientControlMW.getRoomConfigFileName());
		} finally {
			saveLock.unlock();
		}
	}


	public static void commitTransaction(RoomConfiguration config, String fileName) throws IOException {
		try {
			saveRoomConfiguration(config, fileName);
		} finally {
			saveLock.unlock();
		}
	}


	public static void revertTransaction(RoomConfiguration roomConfig) {
		saveLock.unlock();
	}


	public static RoomConfiguration getRoomConfigByName(String configuration) throws FileNotFoundException {
		XStream xstream = getXStream();
		FileInputStream input = new FileInputStream(DATA_DIRECTORY + File.separator + configuration + ".xml");
		RoomConfiguration result = (RoomConfiguration) xstream.fromXML(input);

		return result;
	}


	private static void saveRoomConfiguration(RoomConfiguration roomConfiguration, String fileName) throws IOException {
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
		xstream.processAnnotations(ActorConfiguration.class);
		xstream.processAnnotations(ActorConfiguration.class);
		xstream.processAnnotations(StripeConfiguration.class);
		xstream.processAnnotations(DeviceConfiguration.class);
		xstream.processAnnotations(SwitchDeviceOverEthernetConfiguration.class);
		xstream.processAnnotations(SimpleColorRenderingProgramConfiguration.class);
		xstream.processAnnotations(SunSetRenderingProgrammConfiguration.class);
		xstream.processAnnotations(TronRenderingProgrammConfiguration.class);
		return xstream;
	}
}
