package org.ambientlight.room;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.ambientlight.AmbientControlMW;
import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.device.drivers.SwitchDeviceOverEthernetConfiguration;
import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.config.device.led.StripePartConfiguration;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.led.ActorConfiguration;
import org.ambientlight.config.room.entities.led.LightObjectConfiguration;
import org.ambientlight.config.room.entities.led.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.led.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.led.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.remoteswitches.RemoteSwitchConfiguration;

import com.thoughtworks.xstream.XStream;


public class RoomConfigurationFactory {

	private static ReentrantLock saveLock = new ReentrantLock();

	static String DATA_DIRECTORY = System.getProperty("user.home") + File.separator + "ambientlight" + File.separator
			+ "sceneries";


	public static void beginTransaction() {
		saveLock.lock();
	}


	public static void commitTransaction() {
		try {
			saveRoomConfiguration(AmbientControlMW.getRoom().config, AmbientControlMW.getRoomConfigFileName());
		} catch (IOException e) {
			System.out.println("RoomConfigurationFactory - commitTransaktion(): Error writing roomConfiguration to Disk!");
			System.exit(1);

		} finally {
			saveLock.unlock();
		}
	}


	public static void commitTransaction(RoomConfiguration config, String fileName) {
		try {
			saveRoomConfiguration(config, fileName);
		} catch (IOException e) {
			System.out.println("RoomConfigurationFactory - commitTransaktion(): Error writing roomConfiguration to Disk!");
			System.exit(1);

		} finally {
			saveLock.unlock();
		}
	}


	public static void cancelTransaction() {
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
		xstream.processAnnotations(RemoteSwitchConfiguration.class);
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
