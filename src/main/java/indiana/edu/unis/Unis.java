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
package indiana.edu.unis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONObject;

import indiana.edu.handle.HandleSystem;
import indiana.edu.property.Property;

public class Unis {
	
	private HandleSystem handleSystem;
	private String unisPath;
	private String unisService;
	
	public Unis (Property property) throws IOException {
		this.unisPath = property.property.getProperty("unis.path");
		String unis = property.property.getProperty("unis");
		this.unisService = property.property.getProperty("unis.service");
	}
	
	/*
	 * download or upload operation for Unis object
	 */
	public String runUnis(Boolean upload, String filePath, String unisRef, Boolean replicate) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		if (upload) {
			
			if (replicate) {
				Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cd "+this.unisPath +"; source bin/activate; cd libdlt; pid_xfer --replicas 3 " + filePath + " "+this.unisService});
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				String line = null;
				while ((line = reader.readLine()) != null) {
				    System.out.println(line + "\n");
				    result.add(line);
				}
				return result.get(result.size()-1);
			}else {
				Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cd "+this.unisPath +"; source bin/activate; cd libdlt; pid_xfer --replicas 1 " + filePath + " "+this.unisService});
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				String line = null;
				while ((line = reader.readLine()) != null) {
				    System.out.println(line + "\n");
				    result.add(line);
				}
				return result.get(result.size()-1);
			}

		}else {
			System.out.println("download");
			Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cd "+this.unisPath +"; source bin/activate; cd libdlt; pid_xfer " + unisRef +" "+ filePath});
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
			    System.out.println(line + "\n");
			    result.add(line);
			}
			return "";
		}				
	}

}
