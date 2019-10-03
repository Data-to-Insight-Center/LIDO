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
package indiana.edu.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import indiana.edu.data.ReadData;
import indiana.edu.dtr.DataTypeRegistry;
import indiana.edu.dtr.Type;
import indiana.edu.handle.HandleSystem;
import indiana.edu.property.Property;
import indiana.edu.unis.Unis;

public class MetadataRepository {

	private MongoDB mongo;
	private Filesystem filesystem;
	private HandleSystem handle;
	private Unis unis;
	private Property property;
	private String localService;
	private DataTypeRegistry dtr;
	
	public MetadataRepository(Property property) throws IOException {
		this.property = property;
		this.localService = this.property.property.getProperty("local.service");
		System.out.println(this.localService);
		this.unis = new Unis(property);
		this.handle = new HandleSystem(property);
		this.dtr = new DataTypeRegistry(property);
		if (property.property.getProperty("repo").equalsIgnoreCase("MongoDB")) {
			this.mongo = new MongoDB();
			this.filesystem = new Filesystem(property);
		}else {
			this.filesystem = new Filesystem(property);
		}
	}
	
	/*
	 * read the raw document, create the metadata instances in the MongoDB, register the PID for metadata instances
	 * @param filePath: the raw document
	 * @param unisPID: the PID of the Unis object
	 * @return the metadata PID, especially the schema.org dataset metadata instance PID
	 */
	public Map<String, String> insertInstanceMongoDB(String filePath, String unisPID) throws Exception {
		ReadData data = new ReadData();
		Hashtable<String, String> content = data.readContent(filePath);
		
		//create PID for metadata instances
		String sciencePID = this.handle.handlePrefix + "/icnpid." + UUID.randomUUID().toString();
		String sensorPID = this.handle.handlePrefix + "/icnpid." + UUID.randomUUID().toString();
		String datasetPID = this.handle.handlePrefix + "/icnpid." + UUID.randomUUID().toString();

		String location = "Cordra:science:"+sciencePID;
		
		JSONObject scienceKI = this.handle.instanceKI(sciencePID, location, "MongoDB", "science instancec");
		handle.createHandle(scienceKI);
		
		location = "Cordra:sensor:"+sensorPID;
		JSONObject sensorKI = this.handle.instanceKI(sensorPID, location, "MongoDB", "sensor instance");
		this.handle.createHandle(sensorKI);
		
		
		location = "Cordra:dataset:"+datasetPID;
		JSONObject datasetKI = this.handle.instanceKI(datasetPID, location, "MongoDB", "dataset instance");
		this.handle.createHandle(datasetKI);
		
		//create metadata instances in MongoDB
		InstanceJSON input = new InstanceJSON();
		JSONObject SCIENCE = input.SCIENCEinstance(sciencePID, content.get("file name"), 
				content.get("sensor"), content.get("device"), content.get("date"), content.get("date"), content.get("geolocation"),
				"Raw", sciencePID, sensorPID, datasetPID);
		
		
	
		JSONObject SENSOR = input.SENSORinstance(sensorPID, content.get("file name"), content.get("file type"), 
				content.get("reading type"), content.get("reading value"),  
				content.get("size"), content.get("reading structure"), sciencePID, sensorPID, datasetPID);
		
		
		JSONObject DATASET = input.DATASETinstance(datasetPID, content.get("file name"), 
				"This is a test schema.org instance for airbox data", "Sesnor reading/per day", 
				new JSONArray(), content.get("geolocation"), 
				content.get("date"), content.get("sensor"), "Unis", 
				unisPID, content.get("file type"), sciencePID, sensorPID, datasetPID);
		
		
		this.mongo.insertDocument("sensor", SENSOR);
		this.mongo.insertDocument("science", SCIENCE);
		this.mongo.insertDocument("dataset", DATASET);
		this.mongo.close();
		//done
		Map<String, String> result = new HashMap<String, String>();
		result.put("science", sciencePID);
		result.put("sensor", sensorPID);
		result.put("dataset", datasetPID);
		return result;
			
	}
	
	/*
	 * read the raw document, create the metadata instances in the file system, register the PID for metadata instances
	 * @param filePath: the raw document
	 * @param unisPID: the PID of the Unis object
	 * @return the metadata PID, especially the schema.org dataset metadata instance PID
	 */
	public Map<String, String> insertInstanceFileSystem(String filePath, String unisPID) throws Exception {
		ReadData data = new ReadData();
		Hashtable<String, String> content = data.readContent(filePath);
		String name = content.get("file name")+".json";
		
		//create PID for metadata instances
		String sciencePID = this.handle.handlePrefix + "/icnpid." + UUID.randomUUID().toString();
		String sensorPID = this.handle.handlePrefix + "/icnpid." + UUID.randomUUID().toString();
		String datasetPID = this.handle.handlePrefix + "/icnpid." + UUID.randomUUID().toString();
		
		String location = this.filesystem.SCIENCE_FOLDER+name;
		JSONObject scienceKI = this.handle.instanceKI(sciencePID, location, "FileSystem", "science instance");
		handle.createHandle(scienceKI);
		
		location = this.filesystem.SENSOR_FOLDER+name;
		JSONObject sensorKI = this.handle.instanceKI(sensorPID, location, "FileSystem", "sensor instance");
		this.handle.createHandle(sensorKI);
		
		location = this.filesystem.DATASET_FOLDER+name;
		JSONObject datasetKI = this.handle.instanceKI(datasetPID, location, "FileSystem", "dataset instance");
		this.handle.createHandle(datasetKI);
			
		//create metadata instances in file system
		InstanceJSON input = new InstanceJSON();
		JSONObject SCIENCE = input.SCIENCEinstance(sciencePID, content.get("file name"), 
				content.get("sensor"), content.get("device"), content.get("date"), content.get("date"), content.get("geolocation"),
				"Raw", sciencePID, sensorPID, datasetPID);
	
	
		JSONObject SENSOR = input.SENSORinstance(sensorPID, content.get("file name"), content.get("file type"), 
				content.get("reading type"), content.get("reading value"),  
				content.get("size"), content.get("reading structure"), sciencePID, sensorPID, datasetPID);
		
		
		JSONObject DATASET = input.DATASETinstance(datasetPID, content.get("file name"), 
				"This is a test schema.org instance for airbox data", "Sesnor reading/per day", 
				new JSONArray(), content.get("geolocation"), 
				content.get("date"), content.get("sensor"), "Unis", 
				unisPID, content.get("file type"), sciencePID, sensorPID, datasetPID);
		
		
		
		this.filesystem.writeFile(name, SCIENCE.toString(), this.filesystem.SCIENCE_FOLDER);
		this.filesystem.writeFile(name, SENSOR.toString(), this.filesystem.SENSOR_FOLDER);
		this.filesystem.writeFile(name, DATASET.toString(), this.filesystem.DATASET_FOLDER);
		//done
		Map<String, String> result = new HashMap<String, String>();
		result.put("science", sciencePID);
		result.put("sensor", sensorPID);
		result.put("dataset", datasetPID);
		return result;
			
	}
	
	/*
	 * use PID of unis object to download the raw document, extract the metadata instances from MongoDB, generate the result bundle, and assign PID for the result bundle
	 * @param unisPID: the PID of the Unis object
	 * @param unisRef: the location of the Unis object
	 * @param datasetKI: the PID KI of schema.org dataset metadata instance
	 * @return the result of the query operation
	 */
	public Map<String, String> queryUnisInstanceMongoDB(String unisPID, String unisRef, JSONObject datasetKI) throws Exception {
		String datasetInstanceLocation = datasetKI.getString("digitalObjectLocation");
		String[] datasetInfo = datasetInstanceLocation.split(":");
		String datasetPID = datasetInfo[2];
		try {
			//abstract object (abstract object) exists in the local LIDO service
			JSONObject datasetInstance = this.mongo.findDocument(datasetInfo[1], datasetInfo[2], null);
			
			
			String sensorPID = datasetInstance.getString(Type.SENSORPID);
			JSONObject sensorKI = this.handle.httpResolve(sensorPID);
			String[] sensorInfo = sensorKI.getString("digitalObjectLocation").split(":");
			JSONObject sensorInstance =this.mongo.findDocument(sensorInfo[1], sensorInfo[2], null);
			
			String sciencePID = datasetInstance.getString(Type.SCIENCEPID);
			JSONObject scienceKI = this.handle.httpResolve(sciencePID);
			String[] scienceInfo = scienceKI.getString("digitalObjectLocation").split(":");
			JSONObject scienceInstance = this.mongo.findDocument(scienceInfo[1], scienceInfo[2], null);
			
			String name = datasetInstance.getString("name");
			String fileType = datasetInstance.getString(Type.DISTRIBUTIONFILEFORMAT);
			
			
			
			String tempDownload = this.filesystem.DOWNLOAD_FOLDER+name;
			String filePath = tempDownload+"/"+name+fileType;
			File temp = new File(tempDownload);
			temp.mkdir();
			filePath = filePath.replace(" ", "\\ ");
			//download the unis object
			this.unis.runUnis(false, filePath, unisRef, false);
			//extract metadata instances
			this.filesystem.writeFile("ScienceInstance.json", dtr.newOutput(scienceInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("SensorInstance.json", dtr.newOutput(sensorInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("DatasetInstance.json", dtr.newOutput(datasetInstance).toString(), tempDownload+"/");
			//generate result bundle
			this.filesystem.zipFolder(tempDownload, tempDownload+".zip");
			
			Map<String, String> allParams = new HashMap<String, String>();
			allParams.put("datasetPID", datasetPID);		
			allParams.put("PID to Unis object", unisPID);
			
			JSONObject resultKI = this.handle.resultKI(tempDownload+".zip", "Result Bundle", allParams, this.localService+"?name="+datasetInstance.getString("name")+".zip");
			
			
			String resultPID = this.handle.createHandle(resultKI);
			Map<String, String> result = new HashMap<String, String>();
			result.put("resultPID", resultPID);
			result.put("status", "Success");
			
			String device = scienceInstance.getString("name").split(" ")[0];
			String sensor = scienceInstance.getString(Type.SENSORTYPE);
			String day = scienceInstance.getString(Type.STARTDATE);
			
			result.put("device", device);
			result.put("sensor", sensor);
			result.put("day", day);

			filesystem.deleteFolder(tempDownload);
			return result;
		}catch (Exception e) {
			//abstract object doesn't exist in the local LIDO service, regenerate the abstract object
			JSONObject pidKI = this.handle.httpResolve(unisPID);
			String unisLocaton = pidKI.getString("digitalObjectLocation");
			
			String url = unisLocaton;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject unisInstance = new JSONObject(response.toString());
			
			String[] tempName = unisInstance.getString("name").split("/");
			String realName = tempName[tempName.length-1];
			
			String uploadPath = property.property.getProperty("UPLOADED_FOLDER")+realName;
			this.unis.runUnis(false, uploadPath, unisLocaton, false);
			
			Map<String, String> reInsert = new HashMap<String, String>();
			if (!property.property.getProperty("repo").equalsIgnoreCase("MongoDB")) {
				//regenerate the abstract object
				reInsert = this.insertInstanceFileSystem(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				//query
				datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				
				return this.queryUnisInstanceFileSystem(unisPID, unisRef, datasetKI);
			}else {
				//regenerate the abstract object
				reInsert = this.insertInstanceMongoDB(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				//query
				datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				
				return this.queryUnisInstanceMongoDB(unisPID, unisRef, datasetKI);
			}
		
		}
	}
	
	/*
	 * use PID of unis object to download the raw document, extract the metadata instances from file system, generate the result bundle, and assign PID for the result bundle
	 * @param unisPID: the PID of the Unis object
	 * @param unisRef: the location of the Unis object
	 * @param datasetKI: the PID KI of schema.org dataset metadata instance
	 * @return the result of the query operation
	 */
	public Map<String, String> queryUnisInstanceFileSystem(String unisPID, String unisRef, JSONObject datasetKI) throws Exception {
		String datasetInstanceLocation = datasetKI.getString("digitalObjectLocation");
		File tempFile = new File(datasetInstanceLocation);
		if (tempFile.exists()) {
			//abstract object (metadata instances) exists in the local LIDO service
			JSONObject datasetInstance = this.filesystem.readFile(datasetInstanceLocation);
			String datasetPID = datasetInstance.getString("id");
			
			String sensorPID = datasetInstance.getString(Type.SENSORPID);
			JSONObject sensorKI = this.handle.httpResolve(sensorPID);
			String sensorLocation = sensorKI.getString("digitalObjectLocation");
			JSONObject sensorInstance = this.filesystem.readFile(sensorLocation);
			
			String sciencePID = datasetInstance.getString(Type.SCIENCEPID);
			JSONObject scienceKI = this.handle.httpResolve(sciencePID);
			String scienceLocation = scienceKI.getString("digitalObjectLocation");
			JSONObject scienceInstance = this.filesystem.readFile(scienceLocation);
			
			String name = datasetInstance.getString("name");
			String fileType = datasetInstance.getString(Type.DISTRIBUTIONFILEFORMAT);
			
			String tempDownload = this.filesystem.DOWNLOAD_FOLDER+name;
			String filePath = tempDownload+"/"+name+fileType;
			File temp = new File(tempDownload);
			temp.mkdir();
			filePath = filePath.replace(" ", "\\ ");
			
			//download the unis object
			this.unis.runUnis(false, filePath, unisRef, false);
			//extract the metadata instances
			this.filesystem.writeFile("ScienceInstance.json", dtr.newOutput(scienceInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("SensorInstance.json", dtr.newOutput(sensorInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("DatasetInstance.json", dtr.newOutput(datasetInstance).toString(), tempDownload+"/");
			//generate the result bundle
			this.filesystem.zipFolder(tempDownload, tempDownload+".zip");
			
			Map<String, String> allParams = new HashMap<String, String>();
	
			allParams.put("datasetPID", datasetPID);
			allParams.put("PID to Unis object", unisPID);
			
			JSONObject resultKI = this.handle.resultKI(tempDownload+".zip", "Result Bundle", allParams, this.localService+"?name="+datasetInstance.getString("name")+".zip");

			String resultPID = this.handle.createHandle(resultKI);
			Map<String, String> result = new HashMap<String, String>();
			result.put("resultPID", resultPID);
			result.put("status", "Success");
			
	
			String device = scienceInstance.getString("name").split(" ")[0];
			String sensor = scienceInstance.getString(Type.SENSORTYPE);
			String day = scienceInstance.getString(Type.STARTDATE);
			
			result.put("device", device);
			result.put("sensor", sensor);
			result.put("day", day);
			
			filesystem.deleteFolder(tempDownload);
			return result;
		}else {
			//abstract object doesn't exist in the local LIDO service, regenerate the abstract object
			JSONObject pidKI = this.handle.httpResolve(unisPID);
			String unisLocaton = pidKI.getString("digitalObjectLocation");
			
			String url = unisLocaton;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");

			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject unisInstance = new JSONObject(response.toString());
			
			String[] tempName = unisInstance.getString("name").split("/");
			String realName = tempName[tempName.length-1];
			
			String uploadPath = property.property.getProperty("UPLOADED_FOLDER")+realName;
			this.unis.runUnis(false, uploadPath, unisLocaton, false);
			Map<String, String> reInsert = new HashMap<String, String>();
			if (!property.property.getProperty("repo").equalsIgnoreCase("MongoDB")) {
				//regenerate the abstract object
				reInsert = this.insertInstanceFileSystem(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				
				datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				//query
				return this.queryUnisInstanceFileSystem(unisPID, unisRef, datasetKI);
			}else {
				//regenerate the abstract object
				reInsert = this.insertInstanceMongoDB(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				
				datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				//query
				return this.queryUnisInstanceMongoDB(unisPID, unisRef, datasetKI);
			}
		}
	}
	
	/*
	 * use PID KI of metadata instances (schema.org dataset metadata instance) to query the abstract object
	 * @param unisPID: the PID of the Unis object
	 * @param instanceKI: the PID KI of the metadata instance
	 * @return the query result including the PID of the result bundle 
	 */
	public Map<String, String> queryOneInstanceMongoDB(String unisPID, JSONObject instanceKI) throws Exception {
		String location = instanceKI.getString("digitalObjectLocation");
		String[] locationInfo = location.split(":");
		try {
			//abstract object exists
			JSONObject instance = this.mongo.findDocument(locationInfo[1], locationInfo[2], null); 
			String sensorPID = instance.getString(Type.SENSORPID);
			String sciencePID = instance.getString(Type.SCIENCEPID);
			String datasetPID = instance.getString(Type.DATASETPID);
						
			JSONObject datasetKI = this.handle.httpResolve(datasetPID);
			
			String datasetInstanceLocation = datasetKI.getString("digitalObjectLocation");
			String[] datasetInfo = datasetInstanceLocation.split(":");
			JSONObject datasetInstance = this.mongo.findDocument(datasetInfo[1], datasetInfo[2], null);			
		
			JSONObject sensorKI = this.handle.httpResolve(sensorPID);
			String[] sensorInfo = sensorKI.getString("digitalObjectLocation").split(":");
			JSONObject sensorInstance =this.mongo.findDocument(sensorInfo[1], sensorInfo[2], null);		

			JSONObject scienceKI = this.handle.httpResolve(sciencePID);
			String[] scienceInfo = scienceKI.getString("digitalObjectLocation").split(":");
			JSONObject scienceInstance = this.mongo.findDocument(scienceInfo[1], scienceInfo[2], null);
			
			String name = datasetInstance.getString("name");
			String fileType = datasetInstance.getString(Type.DISTRIBUTIONFILEFORMAT);
					
			
			String tempDownload = this.filesystem.DOWNLOAD_FOLDER+name;
			String filePath = tempDownload+"/"+name+fileType;
			filePath = filePath.replace(" ", "\\ ");
			File temp = new File(tempDownload);
			temp.mkdir();
			
			String unisRef = this.handle.httpResolve(datasetInstance.getString(Type.DISTRIBUTIONCONTENTURL)).getString("digitalObjectLocation");
			
			//download the unis object
			this.unis.runUnis(false, filePath, unisRef, false);
			//extract the metadata instances
			this.filesystem.writeFile("ScienceInstance.json", dtr.newOutput(scienceInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("SensorInstance.json", dtr.newOutput(sensorInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("DatasetInstance.json", dtr.newOutput(datasetInstance).toString(), tempDownload+"/");
			//generate the result bundle
			this.filesystem.zipFolder(tempDownload, tempDownload+".zip");
			
			Map<String, String> allParams = new HashMap<String, String>();

			allParams.put("datasetPID", datasetPID);
			allParams.put("PID to Unis object", unisPID);
			
			JSONObject resultKI = this.handle.resultKI(tempDownload+".zip", "Result Bundle", allParams, this.localService+"?name="+datasetInstance.getString("name")+".zip");
			
			String resultPID = this.handle.createHandle(resultKI);
			Map<String, String> result = new HashMap<String, String>();
			result.put("resultPID", resultPID);			
			result.put("status", "Success");
			
			String device = scienceInstance.getString("name").split(" ")[0];
			String sensor = scienceInstance.getString(Type.SENSORTYPE);
			String day = scienceInstance.getString(Type.STARTDATE);
			
			result.put("device", device);
			result.put("sensor", sensor);
			result.put("day", day);
			
			filesystem.deleteFolder(tempDownload);
			return result;
			
		}catch (Exception e){
			//abstract object doesn't exist, regenerate the abstract object in local LIDO
			JSONObject pidKI = this.handle.httpResolve(unisPID);
			unisPID = pidKI.getString("PID to Unis object");
			pidKI = this.handle.httpResolve(unisPID);
			String unisLocaton = pidKI.getString("digitalObjectLocation");
			
			String url = unisLocaton;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");

			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject unisInstance = new JSONObject(response.toString());
			
			String[] tempName = unisInstance.getString("name").split("/");
			String realName = tempName[tempName.length-1];
			
			String uploadPath = property.property.getProperty("UPLOADED_FOLDER")+realName;
			this.unis.runUnis(false, uploadPath, unisLocaton, false);
			Map<String, String> reInsert = new HashMap<String, String>();
			if (!property.property.getProperty("repo").equalsIgnoreCase("MongoDB")) {
				//regenerate the abstract object
				reInsert = this.insertInstanceFileSystem(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				
				JSONObject datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				//query
				return this.queryOneInstanceFileSystem(unisPID, datasetKI);
			}else {
				//regenerate the abstract object
				reInsert = this.insertInstanceMongoDB(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				
				JSONObject datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				//query
				return this.queryOneInstanceMongoDB(unisPID, datasetKI);
			}
					
		}
	
	}
	
	/*
	 * use PID KI of metadata instances (schema.org dataset metadata instance) to query the abstract object
	 * @param unisPID: the PID of the Unis object
	 * @param instanceKI: the PID KI of the metadata instance
	 * @return the query result including the PID of the result bundle 
	 */
	public Map<String, String> queryOneInstanceFileSystem(String unisPID, JSONObject instanceKI) throws Exception {
		String location = instanceKI.getString("digitalObjectLocation");
		File tempFile = new File(location);
		if(tempFile.exists()) {
			//abstract object exists
			JSONObject instance = this.filesystem.readFile(location);
				
			String sensorPID = instance.getString(Type.SENSORPID);
			String sciencePID = instance.getString(Type.SCIENCEPID);
			String datasetPID = instance.getString(Type.DATASETPID);			
			
			JSONObject datasetKI = this.handle.httpResolve(datasetPID);
			
			String datasetInstanceLocation = datasetKI.getString("digitalObjectLocation");
			JSONObject datasetInstance = this.filesystem.readFile(datasetInstanceLocation);
			
			JSONObject sensorKI = this.handle.httpResolve(sensorPID);
			String sensorLocation = sensorKI.getString("digitalObjectLocation");
			JSONObject sensorInstance = this.filesystem.readFile(sensorLocation);
			
			JSONObject scienceKI = this.handle.httpResolve(sciencePID);
			String scienceLocation = scienceKI.getString("digitalObjectLocation");
			JSONObject scienceInstance = this.filesystem.readFile(scienceLocation);
			
			String name = datasetInstance.getString("name");
			String fileType = datasetInstance.getString(Type.DISTRIBUTIONFILEFORMAT);
	
			
			String tempDownload = this.filesystem.DOWNLOAD_FOLDER+name;
			String filePath = tempDownload+"/"+name+fileType;
			File temp = new File(tempDownload);
			temp.mkdir();
			filePath = filePath.replace(" ", "\\ ");
			String unisRef = this.handle.httpResolve(datasetInstance.getString(Type.DISTRIBUTIONCONTENTURL)).getString("digitalObjectLocation");
			//download the unis object
			this.unis.runUnis(false, filePath, unisRef, false);
			//extract the metadata instances
			this.filesystem.writeFile("ScienceInstance.json", dtr.newOutput(scienceInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("SensorInstance.json", dtr.newOutput(sensorInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("DatasetInstance.json", dtr.newOutput(datasetInstance).toString(), tempDownload+"/");
			//generate the result bundle
			this.filesystem.zipFolder(tempDownload, tempDownload+".zip");
			
			Map<String, String> allParams = new HashMap<String, String>();	
			allParams.put("datasetPID", datasetPID);
			allParams.put("PID to Unis object", unisPID);
					
			JSONObject resultKI = this.handle.resultKI(tempDownload+".zip", "Result Bundle", allParams, this.localService+"?name="+datasetInstance.getString("name")+".zip");
						
			String resultPID = this.handle.createHandle(resultKI);
			Map<String, String> result = new HashMap<String, String>();
			result.put("resultPID", resultPID);		
			result.put("status", "Success");			
	
			String device = scienceInstance.getString("name").split(" ")[0];
			String sensor = scienceInstance.getString(Type.SENSORTYPE);
			String day = scienceInstance.getString(Type.STARTDATE);
			
			result.put("device", device);
			result.put("sensor", sensor);
			result.put("day", day);
			
			filesystem.deleteFolder(tempDownload);
			return result;
		}else {
			//abstract object doesn't exists, regenerate the abstract object in local LIDO
			JSONObject pidKI = this.handle.httpResolve(unisPID);
			unisPID = pidKI.getString("PID to Unis object");
			pidKI = this.handle.httpResolve(unisPID);
			String unisLocaton = pidKI.getString("digitalObjectLocation");
			
			String url = unisLocaton;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");

			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject unisInstance = new JSONObject(response.toString());
			
			String[] tempName = unisInstance.getString("name").split("/");
			String realName = tempName[tempName.length-1];
			
			String uploadPath = property.property.getProperty("UPLOADED_FOLDER")+realName;
			this.unis.runUnis(false, uploadPath, unisLocaton, false);
			Map<String, String> reInsert = new HashMap<String, String>();
			if (!property.property.getProperty("repo").equalsIgnoreCase("MongoDB")) {
				//regenerate the abstract object
				reInsert = this.insertInstanceFileSystem(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				
				JSONObject datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				//query
				return this.queryOneInstanceFileSystem(unisPID, datasetKI);
			}else {
				//regenerate the abstract object
				reInsert = this.insertInstanceMongoDB(uploadPath, unisPID);
				File file = new File(uploadPath);
				file.delete();
				
				JSONObject datasetKI = this.handle.httpResolve(reInsert.get("dataset").toString());
				//query
				return this.queryOneInstanceMongoDB(unisPID, datasetKI);
			}
		}
	}
	
	/*
	 * use device, sensor and day information to query the abstract object, generate the result bundle and assign PID for it
	 * @param allParams: the device, sensor and day information
	 * @return the query result including the PID of the result bundle
	 */
	public Map<String, String>  queryInstanceMongoDB(Map<String,String> allParams) throws Exception {
		String device = allParams.get(Type.DEVICEMODEL);
		String sensor = allParams.get(Type.SENSORTYPE);
		String day = allParams.get(Type.STARTDATE);
		
		String sensorQueryValue = allParams.remove(Type.SENSORTYPE);
		
		JSONObject scienceInstance = this.mongo.findDocument("science", "", allParams);
		Boolean check = false;
		
		String sensorPID = scienceInstance.getString(Type.SENSORPID);
		String datasetPID = scienceInstance.getString(Type.DATASETPID);
		if (scienceInstance.getString(Type.SENSORTYPE).contains(sensorQueryValue)) {
			check = true;
		}
		
		
		if (check) {
			JSONObject sensorInstance = this.mongo.findDocument("sensor", sensorPID, null);
			
			JSONObject datasetInstance = this.mongo.findDocument("dataset", datasetPID, null);
			

			String tempDownload = this.filesystem.DOWNLOAD_FOLDER+datasetInstance.getString("name");
			
			File temp = new File(tempDownload);
			temp.mkdir();
			
			String name = datasetInstance.getString("name");
			String fileType = datasetInstance.getString(Type.DISTRIBUTIONFILEFORMAT);
			String filePath = tempDownload+"/"+name+fileType;
			filePath = filePath.replace(" ", "\\ ");
			String unisRef = this.handle.httpResolve(datasetInstance.getString(Type.DISTRIBUTIONCONTENTURL)).getString("digitalObjectLocation");
			this.unis.runUnis(false, filePath, unisRef, false);
			
			this.filesystem.writeFile("ScienceInstance.json", dtr.newOutput(scienceInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("SensorInstance.json", dtr.newOutput(sensorInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("DatasetInstance.json", dtr.newOutput(datasetInstance).toString(), tempDownload+"/");
			
			this.filesystem.zipFolder(tempDownload, tempDownload+".zip");
			
			
			allParams.put(Type.SENSORTYPE, sensorQueryValue);
			allParams.put("datasetPID", datasetPID);
			
			JSONObject resultKI = this.handle.resultKI(tempDownload+".zip", "Result Bundle", allParams, this.localService+"?name="+datasetInstance.getString("name")+".zip");
			
			String resultPID = this.handle.createHandle(resultKI);
			Map<String, String> result = new HashMap<String, String>();

			
			result.put("device", device);
			result.put("sensor", sensor);
			result.put("day", day);
			
			result.put("resultPID", resultPID);
			
			result.put("status", "Success");
			this.mongo.close();
			
			filesystem.deleteFolder(tempDownload);
			return result;
			
		}else {
			Map<String, String> result = new HashMap<String, String>();
			result.put("resultPID", "Error");
			result.put("download", "Cannot find the matched document");
			result.put("status", "Fail");
			this.mongo.close();
			return result;
		}
	}
	
	
	/*
	 * use device, sensor and day information to query the abstract object, generate the result bundle and assign PID for it
	 * @param allParams: the device, sensor and day information
	 * @return the query result including the PID of the result bundle
	 */
	public Map<String, String> queryInstanceFileSystem(Map<String,String> allParams) throws Exception  {
		String device = allParams.get(Type.DEVICEMODEL);
		String sensor = allParams.get(Type.SENSORTYPE);
		String day = allParams.get(Type.STARTDATE);
		
		String name = device+ " " +day +".json";
		
		
		Boolean checkSensor = false;
		JSONObject scienceInstance = this.filesystem.readFile(name, this.filesystem.SCIENCE_FOLDER);
		JSONObject sensorInstance = this.filesystem.readFile(name, this.filesystem.SENSOR_FOLDER);
		JSONObject datasetInstance = this.filesystem.readFile(name, this.filesystem.DATASET_FOLDER);
		
		String datasetPID = datasetInstance.getString(Type.DATASETPID);
	
		if (scienceInstance.getString(Type.SENSORTYPE).contains(sensor)) {
			checkSensor = true;
		}
		
			
		if ( checkSensor) {
			
			String unisRef = this.handle.httpResolve(datasetInstance.getString(Type.DISTRIBUTIONCONTENTURL)).getString("digitalObjectLocation");
			
			
			String tempDownload = this.filesystem.DOWNLOAD_FOLDER+datasetInstance.getString("name");
			
			File temp = new File(tempDownload);
			temp.mkdir();
			
			String fileName = datasetInstance.getString("name");
			String fileType = datasetInstance.getString(Type.DISTRIBUTIONFILEFORMAT);
			String filePath = tempDownload+"/"+fileName+fileType;
			filePath = filePath.replace(" ", "\\ ");
			this.unis.runUnis(false, filePath, unisRef, false);
			
			this.filesystem.writeFile("ScienceInstance.json", dtr.newOutput(scienceInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("SensorInstance.json", dtr.newOutput(sensorInstance).toString(), tempDownload+"/");
			this.filesystem.writeFile("DatasetInstance.json", dtr.newOutput(datasetInstance).toString(), tempDownload+"/");
			
			this.filesystem.zipFolder(tempDownload, tempDownload+".zip");
			
			
			allParams.put(Type.SENSORTYPE, sensor);
			allParams.put("datasetPID", datasetPID);
			JSONObject resultKI = this.handle.resultKI(tempDownload+".zip", "Result Bundle", allParams, this.localService+"?name="+datasetInstance.getString("name")+".zip");
			
			String resultPID = this.handle.createHandle(resultKI);
			Map<String, String> result = new HashMap<String, String>();
			
			
			result.put("device", device);
			result.put("sensor", sensor);
			result.put("day", day);
			
			result.put("resultPID", resultPID);
			
			result.put("status", "Success");
			
			filesystem.deleteFolder(tempDownload);
			return result;
			
		}else {
			Map<String, String> result = new HashMap<String, String>();
			result.put("resultPID", "Error");
			result.put("download", "Cannot find the matched document");
			result.put("status", "Fail");
			return result;
		}
	}
	
	/*
	 * query the PID of schema.org dataset metadata instance to generate the result bundle
	 * @param instanceKI: the schema.org dataset metadata instance
	 * @return the query result including the PID of the result bundle
	 */
	public Map<String, String> resultElementMongoDB(JSONObject instanceKI) throws Exception{
		String location = instanceKI.getString("digitalObjectLocation");
		String[] locationInfo = location.split(":");
		JSONObject instance = this.mongo.findDocument(locationInfo[1], locationInfo[2], null);

		String sciencePID = instance.getString(Type.SCIENCEPID);
		//String datasetPID = instance.getString("test/dfbed2f08f990e9c7c44");
		//JSONObject datasetKI = this.handle.httpResolve(datasetPID);	
		//String datasetInstanceLocation = datasetKI.getString("digitalObjectLocation");
		//String[] datasetInfo = datasetInstanceLocation.split(":");
		//JSONObject datasetInstance = this.mongo.findDocument(datasetInfo[1], datasetInfo[2], null);
		

		JSONObject scienceKI = this.handle.httpResolve(sciencePID);
		String[] scienceInfo = scienceKI.getString("digitalObjectLocation").split(":");
		JSONObject scienceInstance = this.mongo.findDocument(scienceInfo[1], scienceInfo[2], null);

	
		Map<String, String> result = new HashMap<String, String>();		

		String device = scienceInstance.getString("name").split(" ")[0];
		String sensor = scienceInstance.getString(Type.SENSORTYPE);
		String day = scienceInstance.getString(Type.STARTDATE);
		result.put("device", device);
		result.put("sensor", sensor);
		result.put("day", day);
	
		return result;
	}
	
	
	/*
	 * query the PID of schema.org dataset metadata instance to generate the result bundle
	 * @param instanceKI: the schema.org dataset metadata instance
	 * @return the query result including the PID of the result bundle
	 */
	public Map<String, String> resultElementFileSystem(JSONObject instanceKI) throws Exception{
		String location = instanceKI.getString("digitalObjectLocation");
		JSONObject instance = this.filesystem.readFile(location);		

		String sciencePID = instance.getString(Type.SCIENCEPID);
		//String datasetPID = instance.getString("test/dfbed2f08f990e9c7c44");			
		//JSONObject datasetKI = this.handle.httpResolve(datasetPID);
		//String datasetInstanceLocation = datasetKI.getString("digitalObjectLocation");
		//JSONObject datasetInstance = this.filesystem.readFile(datasetInstanceLocation);
		
		JSONObject scienceKI = this.handle.httpResolve(sciencePID);
		String scienceLocation = scienceKI.getString("digitalObjectLocation");
		JSONObject scienceInstance = this.filesystem.readFile(scienceLocation);
		
		Map<String, String> result = new HashMap<String, String>();		

		String device = scienceInstance.getString("name").split(" ")[0];
		String sensor = scienceInstance.getString(Type.SENSORTYPE);
		String day = scienceInstance.getString(Type.STARTDATE);
		result.put("device", device);
		result.put("sensor", sensor);
		result.put("day", day);
		

		return result;
	}

}
