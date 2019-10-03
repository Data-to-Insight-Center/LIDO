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
package indiana.edu.DOA;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import indiana.edu.data.QueryResponse;
import indiana.edu.dtr.Type;
import indiana.edu.handle.HandleSystem;
import indiana.edu.metadata.MetadataRepository;
import indiana.edu.property.Property;
import indiana.edu.unis.Unis;

public class LIDO {
	
	private Property property;
	private MetadataRepository metadata;
	private HandleSystem handle;
	
	public LIDO(Property property) throws IOException {
		this.property = property;
		this.handle = new HandleSystem(property);
		metadata = new MetadataRepository(property);
	}
	
	/*
	 * manage the uploading operations. Upload the document into Unis, generate the metadata instances, register PID for unis object, schema.org
	 * dataset metadata instance
	 * @param uploadLocation: the location of the document in LIDO service
	 * @param replicate: the choice of making replications
	 * @return the result of uploading operation, including the PID of the Unis object
	 */
	public Map<String, String> uploadManager(String uploadLocation, Boolean replicate) throws Exception {
		
		Unis unis = new Unis(property);
		Map<String, String> result = new HashMap<String, String>();
		String unisLink = unis.runUnis(true, uploadLocation, null, replicate);
		System.out.println("luoyu "+unisLink);
		String uuid = UUID.randomUUID().toString();
		String unisPID = this.handle.handlePrefix + "/icnpid." + uuid;
		
		HandleSystem handle = new HandleSystem(property);

		
		if (property.property.getProperty("repo").equalsIgnoreCase("MongoDB")) {
			result =  metadata.insertInstanceMongoDB(uploadLocation, unisPID);			
		}else {
			result =  metadata.insertInstanceFileSystem(uploadLocation, unisPID);			
		}
		
		JSONObject pidKI = handle.pidKI(unisPID, unisLink, "unis", result.get("dataset"));
		
		handle.createHandle(pidKI);

		
		result.put("unisPIDs", unisPID);
		return result;
	}
	
	/*
	 * Manager the query operations. Check the PID KI, find the relative metadata instance and Unis object, generate the result bundle and register PID for
	 * the result bundle 
	 * @param pid: the PID of Unis object or the result bundle 
	 * @param allParams: the device, sensor and day parameters
	 * @return queryResponse: the query result with PID of the result bundle
	 */
	public QueryResponse queryManager(String pid, Map<String,String> allParams) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		
		if (pid != null && pid != "") {
			//query the PID
			JSONObject pidKI = this.handle.httpResolve(pid);

			if (pidKI.getString("etag").equalsIgnoreCase("Result Bundle")) {
				// query the PID of the result bundle		
				String location = pidKI.getString("digitalObjectLocation");
				
				File zip = new File(location);			
				if (zip.exists()) {
					//result bundle is existing in the local LIDO system
					JSONObject datasetKI = this.handle.httpResolve(pidKI.getString("datasetPID"));					
					String datasetInstanceRepo = datasetKI.getString("meta repo");
					
					if (datasetInstanceRepo.equalsIgnoreCase("FileSystem")) {
						//query in filesystem
						result = this.metadata.resultElementFileSystem(datasetKI);
				
					}else {
						//query in mongodb
						result = this.metadata.resultElementMongoDB(datasetKI);
					}
					result.put("resultPID", pid);
					result.put("status", "Success");
				}else {
					//the result bundle (abstract object) is not existed in the local LIDO service
					JSONObject datasetKI = this.handle.httpResolve(pidKI.getString("datasetPID"));
					String repo = datasetKI.getString("meta repo");				
					
					if (repo.equalsIgnoreCase("FileSystem")) {
						//query in file system
						result = this.metadata.queryOneInstanceFileSystem(pid, datasetKI);
					}else {
						//query in MongoDB
						result = this.metadata.queryOneInstanceMongoDB(pid, datasetKI);
					}	
				}
				
			}
			else if (pidKI.getString("etag").equalsIgnoreCase("unis")) {
				//query the PID of the Unis object
				JSONObject datasetKI = this.handle.httpResolve(pidKI.getString("datasetPID"));

				String datasetInstanceRepo = datasetKI.getString("meta repo");
				
				if (datasetInstanceRepo.equalsIgnoreCase("FileSystem")) {
					//query in file system
					result = this.metadata.queryUnisInstanceFileSystem(pid, pidKI.getString("digitalObjectLocation"), datasetKI);			
				}else {
					//query in MongoDB
					result = this.metadata.queryUnisInstanceMongoDB(pid, pidKI.getString("digitalObjectLocation"), datasetKI);
				}
				
			} else {
				//query the PID of the metadata instance
				String repo = pidKI.getString("meta repo");
				
				if (repo.equalsIgnoreCase("FileSystem")) {
					result = this.metadata.queryOneInstanceFileSystem(pid, pidKI);
				}else {
					result = this.metadata.queryOneInstanceMongoDB(pid, pidKI);
				}	
			}
			
			return new QueryResponse(result.get("device"), result.get("sensor"), result.get("day"), result.get("resultPID"), result.get("status"));
		}
		
		
		else if (property.property.getProperty("repo").equalsIgnoreCase("MongoDB")) {
			//query by the device, sensor and day parameters in MongoDB 
			
			result = metadata.queryInstanceMongoDB(allParams);
			String device = allParams.get(Type.DEVICEMODEL);
			String sensor = allParams.get(Type.SENSORTYPE);
			String day = allParams.get(Type.STARTDATE);
			
			return new QueryResponse(device, sensor, day, result.get("resultPID"), result.get("status"));
			
		}else {
			//query by the device, sensor and day parameters in file system
			result = metadata.queryInstanceFileSystem(allParams);
			String device = allParams.get(Type.DEVICEMODEL);
			String sensor = allParams.get(Type.SENSORTYPE);
			String day = allParams.get(Type.STARTDATE);
			
			return new QueryResponse(device, sensor, day, result.get("resultPID"), result.get("status"));
			
		}
	}
}
