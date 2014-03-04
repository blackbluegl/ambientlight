package org.ambientlight;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.ambientlight.config.device.drivers.DeviceConfiguration;
import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.config.device.led.StripePartConfiguration;
import org.ambientlight.config.room.RoomConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SimpleColorRenderingProgramConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.SunSetRenderingProgrammConfiguration;
import org.ambientlight.config.room.entities.lightobject.renderingprogram.TronRenderingProgrammConfiguration;
import org.ambientlight.room.entities.lightobject.LightObject;
import org.ambientlight.room.entities.remoteswitches.RemoteSwitch;

import com.thoughtworks.xstream.XStream;


public class Persistence {

	public Persistence(String fileName, RoomConfiguration config) {
		this.fileName = fileName;
		this.roomConfig = config;
	}

	private String fileName;

	private RoomConfiguration roomConfig;

	private final String DATA_DIRECTORY = System.getProperty("user.home") + File.separator + "ambientlight" + File.separator
			+ "sceneries";

	ReentrantLock saveLock = new ReentrantLock();


	public void beginTransaction() {
		saveLock.lock();
	}


	public void commitTransaction() {
		try {
			saveRoomConfiguration();
		} catch (IOException e) {
			System.out.println("RoomConfigurationFactory - commitTransaktion(): Error writing roomConfiguration to Disk!");
			System.exit(1);
		} finally {
			saveLock.unlock();
		}
	}


	public void cancelTransaction() {
		saveLock.unlock();
	}


	public RoomConfiguration getRoomConfigByName(String configuration) throws FileNotFoundException {
		XStream xstream = getXStream();
		FileInputStream input = new FileInputStream(DATA_DIRECTORY + File.separator + configuration + ".xml");
		RoomConfiguration result = (RoomConfiguration) xstream.fromXML(input);

		return result;
	}


	public void saveRoomConfiguration() throws IOException {
		XStream xstream = getXStream();

		String result = xstream.toXML(this.roomConfig);

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
		xstream.processAnnotations(LightObject.class);
		xstream.processAnnotations(RemoteSwitch.class);
		xstream.processAnnotations(StripeConfiguration.class);
		xstream.processAnnotations(DeviceConfiguration.class);
		xstream.processAnnotations(SimpleColorRenderingProgramConfiguration.class);
		xstream.processAnnotations(SunSetRenderingProgrammConfiguration.class);
		xstream.processAnnotations(TronRenderingProgrammConfiguration.class);
		return xstream;
	}
}
