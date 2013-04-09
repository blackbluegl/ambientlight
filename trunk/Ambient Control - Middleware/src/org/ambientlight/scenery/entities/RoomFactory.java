package org.ambientlight.scenery.entities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.drivers.DeviceConfiguration;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.DeviceDriverFactory;
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

public class RoomFactory {

	DeviceDriverFactory deviceFactory;
	
	public RoomFactory(DeviceDriverFactory deviceFactory){
		this.deviceFactory = deviceFactory;
	}
	
	String DATA_DIRECTORY = System.getProperty("user.home") 
			+File.separator+"ambientlight"+File.separator+"sceneries";
			
	
	public RoomConfiguration getRoomConfigByName(String configuration) throws FileNotFoundException {
		XStream xstream = this.getXStream();
		FileInputStream input = new FileInputStream (DATA_DIRECTORY+File.separator+configuration+".xml");
		RoomConfiguration result = (RoomConfiguration)xstream.fromXML(input);

		return result;
	}
	
	public void saveRoomConfiguration(RoomConfiguration roomConfiguration, String fileName) throws IOException{
		XStream xstream = this.getXStream();
		

		
		String result = xstream.toXML(roomConfiguration);
		
		
		File dir = new File(DATA_DIRECTORY);
		if(dir.exists()==false){
			dir.mkdirs();
		}
		
		File file = new File(DATA_DIRECTORY+File.separator+fileName+".xml");
		
		FileWriter fw = new FileWriter(file);
		
		fw.write(result);
		
		fw.flush();
		fw.close();
		
		System.out.println("wrote roomConfig to: "+file.getAbsoluteFile());
	}

	public Room loadRoom(String roomName) throws UnknownHostException, IOException {
		RoomConfiguration roomConfig = getRoomConfigByName(roomName);
		Room room = new Room();
		
		//initialize Pixelmap
		BufferedImage pixelMap = new BufferedImage(roomConfig.width, roomConfig.height, BufferedImage.TYPE_INT_ARGB);
		room.setRoomBitMap(pixelMap);
		
		//initialize the device drivers
		List<DeviceDriver> devices = new ArrayList<DeviceDriver>();
		for(DeviceConfiguration currentDeviceConfig : roomConfig.deviceConfigurations){
			devices.add(this.initializeDevice(currentDeviceConfig));
		}
		room.setDevices(devices);
		
		//initialize the lightObjects
		List<LightObject> lightObjects = new ArrayList<LightObject>();
		for(RoomItemConfiguration currentItemConfiguration : roomConfig.roomItemConfigurations){
			if(currentItemConfiguration instanceof LightObjectConfiguration)
			lightObjects.add(this.initializeLightObject((LightObjectConfiguration)currentItemConfiguration,
					room.getAllStripePartsInRoom()));
		}
		room.setLightObjectsInRoom(lightObjects);
		
		return room;
	}
	
	
	private LightObject initializeLightObject(LightObjectConfiguration lightObjectConfig, 
			List<StripePart> allStripePartsInRoom) {
		List<StripePart> stripePartsInLightObject = 
				this.getStripePartsFromRoomForLightObject(allStripePartsInRoom, lightObjectConfig);
		
		return new LightObject(lightObjectConfig, stripePartsInLightObject);
	}
	
	private List<StripePart> getStripePartsFromRoomForLightObject(
			List<StripePart> stripesInRoom, LightObjectConfiguration configuration) {
		int minPositionX=configuration.xOffsetInRoom;
		int minPositionY=configuration.yOffsetInRoom;
		int maxPoistionX=configuration.xOffsetInRoom+configuration.width;
		int maxPositionY=configuration.yOffsetInRoom+configuration.height;
		
		List<StripePart> result = new ArrayList<StripePart>();
		
		for(StripePart currentSubStripe : stripesInRoom){
			if(minPositionX>currentSubStripe.configuration.startXPositionInRoom){
				continue;
			}
			if(minPositionY>currentSubStripe.configuration.startYPositionInRoom){
				continue;
			}
			if(maxPoistionX<currentSubStripe.configuration.endXPositionInRoom){
				continue;
			}
			if(maxPositionY<currentSubStripe.configuration.endYPositionInRoom){
				continue;
			}
			result.add(currentSubStripe);
		}
		
		return result;
	}
	

	private DeviceDriver initializeDevice(DeviceConfiguration deviceConfig) throws UnknownHostException, IOException{

		DeviceDriver device = deviceFactory.createByName(deviceConfig);
		
		
		return device;
	}
	
	

	
	
	private XStream getXStream(){
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
