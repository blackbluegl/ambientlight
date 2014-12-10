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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * 
 * @author Florian Bornkessel
 * 
 */
public class PlaylistConverter {

	public String getPlayList(String urlString, String channelRegex, String replacement) throws IOException {
		URL url = new URL(urlString);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String line = rd.readLine();

		String result = new String();

		while (line != null) {
			Pattern pattern = Pattern.compile(channelRegex);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String channel = matcher.group(1);
				String newLine = replacement.replace("$$", channel);
				result = result + newLine + "\n";
			} else {
				result = result + line + "\n";
			}
			line = rd.readLine();
		}
		System.out.println(result);
		return result;
	}


	public void savePlayList(String path, String playList) throws IOException {
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		BufferedReader rd = new BufferedReader(new StringReader(playList));
		String line = rd.readLine();

		while (line != null) {
			if (line.isEmpty() == false) {
				writer.println(line);
			}
			line = rd.readLine();
		}
		writer.close();
	}
}
