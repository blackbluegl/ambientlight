package org.radio;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.radio.playist.PlayListRunnable;


/**
 */
@WebServlet(description = "Streams converted radio from tvheadend")
public class Stream extends HttpServlet {

	private static final String PATH = "path";
	private static final String URL = "url";
	private static final String REPLACEMENT = "replacement";
	private static final String CHANNEL_REGEX = "channelRegex";
	private static final String SERVER_URL = "serverUrl";
	private static final String TRANSCODER_COMMAND = "transconderCommand";
	private static final String DEBUG_CONSOLE = "debugConsole";

	private static final long serialVersionUID = 1L;

	PlayListRunnable playListGen;
	String serverUrl = null;
	String[] args = null;
	boolean debug = false;


	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("RadioAdapter: init");
		try {
			// get command
			args = getProperties().getProperty(TRANSCODER_COMMAND).split(" ");

			// get server url
			serverUrl = getProperties().getProperty(SERVER_URL);

			// debug output?
			debug = "true".equals(getProperties().getProperty(DEBUG_CONSOLE));

			// create a playlist for Mediatomb in 1000 seconds intervalls
			String channelRegex = getProperties().getProperty(CHANNEL_REGEX);
			String replacement = getProperties().getProperty(REPLACEMENT);
			String urlString = getProperties().getProperty(URL);
			String path = getProperties().getProperty(PATH);
			playListGen = new PlayListRunnable(channelRegex, replacement, urlString, path, debug);

			System.out.println("RadioAdapter: init playListGenerator");
			new Thread(playListGen, "PlayListGenerator").start();

			System.out.println("RadioAdapter: init complete");
		} catch (Exception e) {
			System.out.println("RadioAdapter - init: did not init propperly");
		}
	}


	@Override
	public void destroy() {
		try {
			playListGen.stop();
			// wait for playListGen to stop
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			System.out.println("playlistGen could not be stopped gracefully!");
			e.printStackTrace();
		}
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String path = request.getPathInfo();
		String channelId = path.substring(path.lastIndexOf('/') + 1);

		try {
			Integer.parseInt(channelId);
		} catch (Exception e) {
			System.out.println("RadioAdapter: no valid channel given!");
			return;
		}

		final String requestUrl = this.serverUrl.replace("$$", channelId);

		System.out.println("Radio Adapter: transcoding client request for: " + requestUrl);

		OutputStream intoProcessStream = null;
		InputStream errorProcessStream = null;
		DataInputStream fromProcessStream = null;
		Process process = null;
		StreamSourceRunnable streamSourceRunnable = null;

		try {
			process = new ProcessBuilder(args).start();

			intoProcessStream = process.getOutputStream();
			fromProcessStream = new DataInputStream(process.getInputStream());
			errorProcessStream = process.getErrorStream();

			// log console output and avoid errorStreamOverflow
			new Thread(new TranscodingLogRunnable(errorProcessStream, debug)).start();

			// // handle input into process in a seperate process
			streamSourceRunnable = new StreamSourceRunnable(intoProcessStream, requestUrl, debug);
			new Thread(streamSourceRunnable).start();

			// write output until stream will be closed by client with an exception
			response.setContentType("audio/mp3");
			while (true) {
				response.getOutputStream().write(fromProcessStream.readUnsignedByte());
			}

		} catch (Exception e) {
			System.out.println("RadioAdapter: stopped streaming");
			if (debug) {
				e.printStackTrace();
			}
		} finally {
			try {

				// stop streaming from server
				if (streamSourceRunnable != null) {
					streamSourceRunnable.stop();
				}

				// handle extra because stream could be closed by datasource error before
				try {
					intoProcessStream.close();
				} catch (Exception e) {

				}

				fromProcessStream.close();
				errorProcessStream.close();
				process.destroy();
			} catch (Exception finallyException) {
				finallyException.printStackTrace();
				// if anything goes wrong here - its ok
			}
		}
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}


	public Properties getProperties() throws IOException {
		File file = new File("/opt/RadioAdapter/config.properties");
		FileInputStream fis = new FileInputStream(file);
		Properties props = new java.util.Properties();
		props.load(fis);
		return props;
	}
}
