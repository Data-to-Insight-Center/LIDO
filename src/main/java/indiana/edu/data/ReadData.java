/*
#
# Copyright 2019 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
*/
package indiana.edu.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class ReadData {
	
	public ReadData() {
		
	}
	
	/*
	 * Read the raw data document, extract all necessary information about the data, e.g. sensor type, device type, sample reading, geolocation.
	 * @param path: the path to the raw document in LIDO service
	 * @return a table of extracted metadata information
	 */
	public Hashtable<String, String> readContent(String path) throws IOException {
		Hashtable<String, String> all = new Hashtable<String, String>();
			
		BufferedReader content = new BufferedReader(new FileReader(path));
		
		String line = content.readLine();
		
		
		if (line.contains("s_t4,s_h4,s_b2,s_d2,s_d0,s_d1,d_t5,d_h5")) {
			all.put("sensor",  "s_t4,s_h4,s_b2,s_d2,s_d0,s_d1,d_t5,d_h5");
		}
		else if (line.contains("s_0,s_1,s_2,s_3,s_d0,s_d1,s_d2,s_t0,s_h0")) {
			all.put("sensor", "s_0,s_1,s_2,s_3,s_d0,s_d1,s_d2,s_t0,s_h0");
		}
		else if (line.contains("s_t4,s_h4,s_b2,s_d2,s_d0,s_d1")) {
			all.put("sensor", "s_t4,s_h4,s_b2,s_d2,s_d0,s_d1");
		}
		else if (line.contains("s_d0,s_d1,s_t4,s_h4")) {
			all.put("sensor", "s_d0,s_d1,s_t4,s_h4");
		}else {
			all.put("sensor", "Error of Sensor information");
		}
		
		String[] items = line.split(",");
		
		int lat = Arrays.asList(items).indexOf("gps_lat");
		int lon = lat + 1;
		int deviceType = Arrays.asList(items).indexOf("app");
		
		ArrayList<String> geolocation = new ArrayList<String>();

		String device = "";
		int count = 0;
		while ((line = content.readLine()) != null ) {
			String[] contents = line.split(",");
			String temp_location = "("+contents[lat]+","+contents[lon]+")";
			if (!geolocation.contains(temp_location)) {
				geolocation.add(temp_location);
			}

			if (count == 0) {
				device = contents[deviceType];
				all.put("reading value", line);
				count = 1;
			}
			
		}
		
		if (!all.containsKey("reading value")) {
			all.put("reading value", "Empty");
		}
		all.put("geolocation", geolocation.toString());

		all.put("device", device);
		
		String[] temp_name = path.split("/");
		String fileName = temp_name[temp_name.length-1];
		String fileType = fileName.substring(fileName.indexOf("."));
		String fileDate = fileName.substring(0, fileName.indexOf("."));
		fileName = device + " " +fileName.substring(0, fileName.indexOf("."));
		File temp_size = new File(path);
		
		all.put("file type", fileType);
		all.put("date", fileDate);
		all.put("file name", fileName);		
		all.put("size", temp_size.length()+" bytes");
		content.close();
	
		all.put("reading type", "Float and Char");		
		all.put("reading structure", "array ");
	
		return all;
	}
	

}
