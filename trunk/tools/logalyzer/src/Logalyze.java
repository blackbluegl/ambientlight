import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


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

/**
 * @author Florian Bornkessel
 * 
 */
public class Logalyze {

	static List<LogEntry> list = new ArrayList<LogEntry>();


	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
"/home/florian/workspace-driver/testrfm22b/Debug/log.tab"));
		try {
			String line = br.readLine();
			line = br.readLine();
			int lineNumber = 1;

			while (line != null) {
				lineNumber++;
				if (lineNumber % 10000 == 0) {
					System.out.println("read lines: " + lineNumber);
				}
				StringTokenizer tokenizer = new StringTokenizer(line, " ");
				String timeStamp = tokenizer.nextToken();
				String value = tokenizer.nextToken();
				int timeStampInt = (int) Math.floor(Double.parseDouble(timeStamp) * 10000);
				int valueInt = Integer.parseInt(value);
				LogEntry entry = new LogEntry();
				entry.timeStamp = timeStampInt;
				entry.state = valueInt;
				if (list.size() == 0) {
					list.add(entry);
				} else {
					LogEntry last = list.get(list.size() - 1);
					if (last.timeStamp == entry.timeStamp || entry.timeStamp == last.timeStamp + 1) {
						list.add(entry);
					} else {
						boolean samplesInserted = false;
						while (samplesInserted == false) {
							LogEntry insertEntry = new LogEntry();
							insertEntry.state = list.get(list.size() - 1).state;
							insertEntry.timeStamp = list.get(list.size() - 1).timeStamp + 1;
							list.add(insertEntry);

							if (list.get(list.size() - 1).timeStamp + 1 == entry.timeStamp) {
								list.add(entry);
								samplesInserted = true;
							}
						}
					}
				}
				line = br.readLine();
			}
		} finally {
			br.close();
		}
		System.out.println("values read");
		// int doubleValues = 0;
		// int failures = 0;
		// for (int i = 0; i < list.size() - 1; i++) {
		// System.out.println(list.get(i).timeStamp);
		//
		// if (list.get(i).timeStamp == list.get(i + 1).timeStamp) {
		// doubleValues++;
		// }
		// if (list.get(i + 1).timeStamp - list.get(i).timeStamp > 1) {
		// failures++;
		// }
		// }
		// System.out.println(doubleValues);
		// System.out.println(failures);
		findSyncWord();
	}


	public static void findSyncWord() {
		int syncWord = 0xc626;
		int syncSize = 16;
		for (int i = 0; i < list.size() - (syncSize - 1); i++) {
			int finding = buildFinding(list.subList(i, i + (syncSize)));
			if (finding == syncWord) {
				System.out.println("finding at: " + list.get(i).timeStamp);
			}
		}
	}


	/**
	 * @param subList
	 * @return
	 */
	private static int buildFinding(List<LogEntry> subList) {
		int result = 0;
		for (int i = 0; i < subList.size(); i++) {
			int shift = subList.size() - 1 - i;
			int mask = subList.get(i).state << shift;
			result = result | mask;
		}
		return result;
	}
}
