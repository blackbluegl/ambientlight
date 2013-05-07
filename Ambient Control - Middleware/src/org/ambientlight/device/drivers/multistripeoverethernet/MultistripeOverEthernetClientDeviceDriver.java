package org.ambientlight.device.drivers.multistripeoverethernet;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.MultiStripeOverEthernetClientDeviceConfiguration;
import org.ambientlight.device.stripe.Stripe;

public class MultistripeOverEthernetClientDeviceDriver implements LedStripeDeviceDriver {

	List<Stripe> stripes = new ArrayList<Stripe>();

	Socket dataSocket = null;
	OutputStream os = null;
	MultiStripeOverEthernetClientDeviceConfiguration configuration;

	@Override
	public void connect() throws UnknownHostException, IOException {

		System.out.println("MultistripeOverEthernetClientDeviceDriver: connecting to: "+this.configuration.hostName+":"+this.configuration.port);

		this.initSession();

		dataSocket = new Socket(this.configuration.hostName,
				this.configuration.port);

		os = dataSocket.getOutputStream();

	}

	private void initSession() throws IOException {
		Socket controlSocket = new Socket(this.configuration.hostName,
				this.configuration.port);

		PrintStream os = new PrintStream(controlSocket.getOutputStream());

		BufferedReader in = new BufferedReader(new InputStreamReader(
				controlSocket.getInputStream()));

		for (Stripe current : stripes) {

			os.println("stripe_port=" + current.configuration.port);

			in = new BufferedReader(new InputStreamReader(
					controlSocket.getInputStream()));

			String stripePortResult = in.readLine();
			if (stripePortResult == null || !"OK".equals(stripePortResult)) {
				throw new IOException("server did not correclty respond!");
			}

			
			os.println("protocoll_type=" + current.configuration.protocollType);

			in = new BufferedReader(new InputStreamReader(
					controlSocket.getInputStream()));

			String protocollType = in.readLine();
			if (protocollType == null || !"OK".equals(protocollType)) {
				throw new IOException("server did not correclty respond!");
			}

			
			os.println("pixel_size=" + current.configuration.pixelAmount);

			in = new BufferedReader(new InputStreamReader(
					controlSocket.getInputStream()));

			String result = in.readLine();
			if (result == null || !"OK".equals(result)) {
				throw new IOException("server did not correclty respond!");
			}
		}
		in.close();
		controlSocket.close();
	}

	@Override
	public void closeConnection() throws IOException {
		{
			os.flush();
			os.close();
			if (dataSocket != null) {
				dataSocket.close();
			}
		}
	}

	@Override
	public void writeData() throws IOException {

		for (int i = 0; i < stripes.size(); i++) {
			Stripe current = stripes.get(i);

			byte[] dataArray = new byte[current.configuration.pixelAmount*3];

			for (int y = 0; y < current.configuration.pixelAmount; y++) {
				Color color = new Color(current.getOutputResult().get(y));
				dataArray[3*y] = (byte) (color.getRed() & 0xFF);
				dataArray[3*y+1] = (byte) (color.getGreen() & 0xFF);
				dataArray[3*y+2] = (byte) (color.getBlue() & 0xFF);
			}

			os.write(dataArray);
		}
	}

	@Override
	public List<Stripe> getAllStripes() {
		return this.stripes;
	}

	@Override
	public void attachStripe(Stripe stripe) {
		this.stripes.add(stripe);
	}

	@Override
	public void setConfiguration(MultiStripeOverEthernetClientDeviceConfiguration configuration) {
		this.configuration = configuration;

		
	}
}
