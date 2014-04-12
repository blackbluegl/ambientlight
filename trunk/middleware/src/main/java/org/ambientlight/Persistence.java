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

	private String fileName;

	private RoomConfiguration roomConfig;

	private int semaphore = 0;


	public Persistence(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		this.roomConfig = loadRoomConfig();
	}

	public static final String DATA_DIRECTORY = System.getProperty("user.home") + File.separator + "ambientlight"
			+ File.separator + "sceneries";

	ReentrantLock saveLock = new ReentrantLock();


	public boolean isTransactionRunning() {
		return saveLock.isLocked();
	}


	public RoomConfiguration getRoomConfiguration() {
		return roomConfig;
	}


	public void beginTransaction() {
		semaphore++;
		if (isTransactionRunning() == false) {
			System.out.println("Persistence - beginTransaction(): beginning transaction.");
			saveLock.lock();
		} else {
			System.out.println("Persistence - beginTransaction(): transaction currently running.");
		}
	}


	public void commitTransaction() {
		try {

			// first check if this transaction is encapsulated in an outer one. do not save the state then
			semaphore = semaphore > 0 ? semaphore - 1 : 0;
			if (semaphore > 0) {
				System.out.println("Persistence - commitTransaktion(): "
						+ "Warning ommitting because an outer transaction is running!");
				return;
			}

			// if no transaction is running because it was canceled. do not save anything to disc
			if (isTransactionRunning() == false) {
				System.out.println("Persistence - commitTransaktion(): Warning no transaction running! No  configuration saved!");
			} else {
				saveLock.unlock();
				saveRoomConfiguration(this.fileName, this.roomConfig);
				System.out.println("Persistence - commitTransaktion(): Successfully saved configuration.");
			}
		} catch (IOException e) {
			System.out
					.println("Persistence - commitTransaktion(): "
					+ "Error writing roomConfiguration to Disk! Emergency exit!");
			System.exit(1);
		}
	}


	public void cancelTransaction() {
		System.out.println("Persistence - cancelTransaction(): Canceling transaction");
		semaphore = 0;
		saveLock.unlock();
	}


	private RoomConfiguration loadRoomConfig() throws FileNotFoundException {
		XStream xstream = getXStream();
		FileInputStream input = new FileInputStream(DATA_DIRECTORY + File.separator + fileName);
		RoomConfiguration result = (RoomConfiguration) xstream.fromXML(input);

		return result;
	}


	public static void saveRoomConfiguration(String fileName, RoomConfiguration config) throws IOException {
		XStream xstream = getXStream();

		String result = xstream.toXML(config);

		File dir = new File(DATA_DIRECTORY);
		if (dir.exists() == false) {
			dir.mkdirs();
		}

		File file = new File(DATA_DIRECTORY + File.separator + fileName);

		FileWriter fw = new FileWriter(file);

		fw.write(result);

		fw.flush();
		fw.close();

		System.out.println("Persistence - saveRoomConfiguration(): wrote roomConfig to: " + file.getAbsoluteFile());
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
