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
package indiana.edu.dtr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import indiana.edu.property.Property;

public class DataTypeRegistry {
	private final String USER_AGENT = "Mozilla/5.0";
	private String URL = "";
	
	public DataTypeRegistry(Property property) {
		this.URL = property.property.getProperty("data.type.registry");
	}
	
	/*
	 * send Get request to Object service (Cordra)
	 * @param id: the type ID
	 * @return the type description
	 */
	public JSONObject getRequest(String id) throws IOException {
		String url = URL+id;
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");

		con.setRequestProperty("User-Agent", USER_AGENT);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		JSONObject output = new JSONObject(response.toString());
		
		return output;
	}
	
	/* query the type description and merge it with the relative value
	 * @param input: the metadata instance
	 * @return the new object of type descriptions and values
	 */
	public JSONArray newOutput (JSONObject input) throws IOException {
		
		JSONArray output = new JSONArray();
		
		for (String key : input.keySet()) {
			if (key.contains("/")) {
				JSONObject value = getRequest(key);
				value.put("value", input.get(key));
				output.put(value);
			}else {
				JSONObject value = new JSONObject();
				value.put(key, input.get(key));
				output.put(value);
			}
		}
		
		return output;
	}
}
