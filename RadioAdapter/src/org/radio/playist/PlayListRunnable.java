/*  Copyright 2013 Florian Bornkessel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.radio.playist;

/**
 * @author Florian Bornkessel
 * 
 */
public class PlayListRunnable implements Runnable {

	private boolean debug = false;

	private boolean run = true;

	private String channelRegex;
	private String replacement;
	private String urlString;
	private String path;


	public PlayListRunnable(String channelRegex, String replacement, String urlString, String path, boolean debug) {
		super();
		this.channelRegex = channelRegex;
		this.replacement = replacement;
		this.urlString = urlString;
		this.path = path;
		this.debug = debug;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		PlaylistConverter pc = new PlaylistConverter();
		while (run) {
			try {
				System.out.println("RadioAdapter: generating playlist for url: " + urlString);

				String playList = pc.getPlayList(urlString, channelRegex, replacement);

				if (debug) {
					System.out.println(playList);
				}

				pc.savePlayList(path, playList);

				if (sleepOrStop())
					return;

			} catch (Exception e) {
				if (run == false) {
					System.out.println("RadioAdapter: Stopped playlistGenerator due an exception");
					return;
				}
				System.out.println("Radio Adapter: retrying creating Playlist in a few minutes");
				if (sleepOrStop())
					return;

			}

		}
	}


	private boolean sleepOrStop() {
		for (int i = 0; i < 1000; i++) {
			try {
				Thread.sleep(500);
			} catch (Exception e2) {
				// should not occour
			}
			if (run == false) {
				System.out.println("RadioAdapter: Stopped playlistGenerator gracefully");
				return true;
			}
		}
		return false;
	}


	public void stop() {
		System.out.println("RadioAdapter: Stopping playlistGenerator");
		run = false;
	}

}
