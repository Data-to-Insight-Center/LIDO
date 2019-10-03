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
package indiana.edu.handle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import indiana.edu.property.Property;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AuthenticationInfo;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.ErrorResponse;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.Util;

public class HandleSystem {
	
	public Properties property = new Properties();
	public String handle;
	public String handlePrefix;
	public String admin_privKey_file;
	public String handle_admin_identifier;
	public String password;
	
	public HandleSystem(Property property) throws IOException{
		this.handle = property.property.getProperty("handle.restful.api.url");
		this.handlePrefix = property.property.getProperty("handle.prefix");
		this.admin_privKey_file = property.property.getProperty("private.key.file");
		this.handle_admin_identifier = property.property.getProperty("handle.admin.identifier");
		this.password = property.property.getProperty("private.key.file.password");
	}
	
	/*
	 * use http to resolve the PID
	 * @param pid: the PID of the object
	 * @return the PID Kernel Information
	 */
	public JSONObject httpResolve(String pidID) throws Exception {
		
		String handleURL = this.handle + pidID;
		URL object = new URL(handleURL);
		HttpURLConnection con = (HttpURLConnection) object.openConnection();
		con.setRequestMethod("GET");
		
		StringBuilder content;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {

            String line;
            content = new StringBuilder();

            while ((line = in.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }

        String jsonString = content.toString();        
        JSONObject output = new JSONObject(jsonString);       
        JSONArray values = (JSONArray) output.get("values");        
        JSONObject newOutput = new JSONObject();
        
        for (Object item : values) {
        	JSONObject value = (JSONObject) item;
        	newOutput.put(value.get("type").toString(), ((JSONObject) value.get("data")).get("value").toString());
        }

		return newOutput;
		
	}
	
	/*
	 * use handle client to resolve the PID
	 * @param pid: the PID of the object
	 * @return the PID Kernel Information
	 */
	public JSONObject handleResolve(String pid) throws HandleException, Exception {
		HandleValue values[] = new HandleResolver().resolveHandle(pid, null, null);
		
		JSONObject newPID = new JSONObject();
		
		for (int i = 0; i < values.length; i++) {
			String[] list = values[i].toString().split(" ");
			String type = list[2].toString().split("=")[1];		
			String[] valueField = values[i].toString().split("\"");
			String value = "";
			if (valueField.length > 1) {
				value = valueField[1];
			}
			newPID.put(type, value);
		}
		
		return newPID;
	
	}
	
	/*
	 * use handle client to create the PID
	 * @param object: the PID Kernel Information
	 * @return the registered PID 
	 */
	public String createHandle(JSONObject object) throws Exception {

		HandleResolver resolver = new HandleResolver();
		
		File privKeyFile = new File(this.admin_privKey_file);
		PrivateKey hdl_adm_priv = net.handle.hdllib.Util.getPrivateKeyFromFileWithPassphrase(privKeyFile, this.password);
		byte adm_handle[] = Util.encodeString(this.handle_admin_identifier);
		AuthenticationInfo auth = new net.handle.hdllib.PublicKeyAuthenticationInfo(adm_handle, 300, hdl_adm_priv);
		
		String handle_identifier = object.getString("PID");
		
		HandleValue[] new_values = new HandleValue[object.keySet().size()];
		
		int count = 0;
		for (Object attribute : object.keySet()){
			HandleValue new_value = new HandleValue(count+1, Util.encodeString(attribute.toString()), Util.encodeString(object.get(attribute.toString()).toString()));
			new_values[count] = new_value;
			count++;
		}
		
		CreateHandleRequest assign_request = new CreateHandleRequest(Util.encodeString(handle_identifier), new_values,
				auth);	
		AbstractResponse response_assign = resolver.processRequestGlobally(assign_request);
		
		if (response_assign.responseCode == AbstractMessage.RC_SUCCESS) {
			return handle_identifier;
		} else {
			byte values[] = ((ErrorResponse) response_assign).message;
			for (int i = 0; i < values.length; i++) {
				System.out.print(String.valueOf(values[i]));
			}
			return "Failed";
		}
	}
	
	
	/*
	 * create the PID Kernel Information
	 * @param pid: the PID for the Kernel Information
	 * @param location: the digital object location
	 * @param etag: the type of the PID
	 * @param datasetPID: the PID of the schema.org dataset metadata instance
	 * @return the PID Kernel Information
	 */
	public JSONObject pidKI(String pid, String location, String etag, String datasetPID) {
		JSONObject pidKI = new JSONObject();
		pidKI.put("PID", pid);
		pidKI.put("KernelInformationProfile", "test");
		pidKI.put("digitalObjectType", "test");
		pidKI.put("digitalObjectLocation", location);
		pidKI.put("digitalObjectPolicy", "test");
		pidKI.put("etag", etag);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		String date = df.format(new Date());
		pidKI.put("dateModified", date);
		pidKI.put("dateCreated", date);
		pidKI.put("version", "0.1");
		pidKI.put("wasDerivedFrom", "null");
		pidKI.put("specializationOf", "null");
		pidKI.put("wasRevisionOf", "null");
		pidKI.put("hadPrimarySource", "null");
		pidKI.put("wasQuotedFrom", "null");
		pidKI.put("alternateOf", "null");
		
		pidKI.put("datasetPID", datasetPID);
		
		return pidKI;
	}
	
	/*
	 * create the metadata instance PID Kernel Information for schema.org dataset metadata instance
	 * @param pid: the PID for the Kernel Information
	 * @param location: the location of the metadata instance
	 * @param repo: the metadata repository information
	 * @param etag: the type of metadata instance
	 * @return the PID Kernel Information
	 */
	public JSONObject instanceKI(String pid, String location, String repo, String etag) {
		JSONObject pidKI = new JSONObject();
		pidKI.put("PID", pid);
		pidKI.put("KernelInformationProfile", "test");
		pidKI.put("digitalObjectType", "test");
		pidKI.put("digitalObjectLocation", location);
		pidKI.put("digitalObjectPolicy", "test");
		pidKI.put("etag", etag);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		String date = df.format(new Date());
		pidKI.put("dateModified", date);
		pidKI.put("dateCreated", date);
		pidKI.put("version", "0.1");
		pidKI.put("wasDerivedFrom", "null");
		pidKI.put("specializationOf", "null");
		pidKI.put("wasRevisionOf", "null");
		pidKI.put("hadPrimarySource", "null");
		pidKI.put("wasQuotedFrom", "null");
		pidKI.put("alternateOf", "null");
		
		pidKI.put("meta repo", repo);

		
		return pidKI;
	}
	
	
	/*
	 * create the PID Kernel Information for the reuslt bundle
	 * @param location: the location of the result bundle
	 * @param etag: the type of the PID KI
	 * @param allParams: the device, sensor and day information
	 * @param url: the url of downloading the bundle
	 * @return the PID KI of the result bundle
	 */
	public JSONObject resultKI(String location, String etag, Map<String, String> allParams, String url) {
		JSONObject pidKI = new JSONObject();
		String uuid = UUID.randomUUID().toString();
		String handle_identifier = this.handlePrefix + "/icnpid." + uuid;
		pidKI.put("PID", handle_identifier);
		pidKI.put("KernelInformationProfile", "test");
		pidKI.put("digitalObjectType", "test");
		pidKI.put("digitalObjectLocation", location);
		pidKI.put("digitalObjectPolicy", "test");
		pidKI.put("etag", etag);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		String date = df.format(new Date());
		pidKI.put("dateModified", date);
		pidKI.put("dateCreated", date);
		pidKI.put("version", "0.1");
		pidKI.put("wasDerivedFrom", "null");
		pidKI.put("specializationOf", "null");
		pidKI.put("wasRevisionOf", "null");
		pidKI.put("hadPrimarySource", "null");
		pidKI.put("wasQuotedFrom", "null");
		pidKI.put("alternateOf", "null");
		for (String key : allParams.keySet()) {
			pidKI.put(key, allParams.get(key));
		}
		
		pidKI.put("URL", url);
		return pidKI;
	}
	
}
