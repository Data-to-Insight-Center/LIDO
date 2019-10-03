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

import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

import indiana.edu.dtr.Type;

public class InstanceJSON {
	
	public Timestamp timestamp;
	
	public InstanceJSON() {
		timestamp = new Timestamp(System.currentTimeMillis());
	}
	
	/*
	 * create the science metadata instance 
	 */
	public JSONObject SCIENCEinstance(String pid, String name, String sensorType, String deviceModel, String startDate, String endDate,
			String geolocation, String status, String sciencePID, String sensorPID, String datasetPID) {
		JSONObject object = new JSONObject();
		object.put("id", pid);
		object.put("name", name);
		object.put("creationDate", timestamp.toString());
		object.put(Type.SENSORTYPE, sensorType);
		object.put(Type.DEVICEMODEL, deviceModel);
		object.put(Type.STARTDATE, startDate);
		object.put(Type.ENDDATE, endDate);
		object.put(Type.GEOLOCATION, geolocation);
		object.put(Type.STATUS, status);
		object.put(Type.SCIENCEPID, sciencePID);
		object.put(Type.SENSORPID, sensorPID);
		object.put(Type.DATASETPID, datasetPID);
		
		return object;
	}
	
	/*
	 * create the schema.org dataset metadata instance
	 */
	public JSONObject DATASETinstance(String pid, String name, String description, String keywords, JSONArray sameAs, String spatialCoverage,
			String temporalCoverage, String variableMeasured, String includedInDataCatalog, String distributionContentUrl,
			String distributionFileFormat, String sciencePID, String sensorPID, String datasetPID) {
		JSONObject object = new JSONObject();
		object.put("id", pid);
		object.put("name", name);
		object.put("creationDate", timestamp.toString());
		object.put(Type.DESCRIPTION, description);
		object.put(Type.KEYWORDS, keywords);
		object.put(Type.SAMEAS, sameAs);
		object.put(Type.SPARIALCOVERAGE, spatialCoverage);
		object.put(Type.TEMPORALCOVERAGE, temporalCoverage);
		object.put(Type.VARIABLEMEASURED, variableMeasured);
		object.put(Type.INCLUDEDINDATACATALOG, includedInDataCatalog);
		object.put(Type.DISTRIBUTIONCONTENTURL, distributionContentUrl);
		object.put(Type.DISTRIBUTIONFILEFORMAT, distributionFileFormat);
		object.put(Type.SCIENCEPID, sciencePID);
		object.put(Type.SENSORPID, sensorPID);
		object.put(Type.DATASETPID, datasetPID);
		return object;
	}
	
	
	/*
	 * create the sensor metadata instance
	 */
	public JSONObject SENSORinstance(String pid, String name, String readingFormat, String readingType, String readingValue,
			String size, String readingStructure, String sciencePID, String sensorPID, String datasetPID) {
		JSONObject object = new JSONObject();
		object.put("id", pid);
		object.put("name", name);
		object.put("creationDate", timestamp.toString());
		object.put(Type.READINGFORMAT, readingFormat);
		object.put(Type.READINGTYPE, readingType);
		object.put(Type.READINGVALUE, readingValue);
		object.put(Type.SIZE, size);
		object.put(Type.READINGSTRUCTURE, readingStructure);	
		object.put(Type.SCIENCEPID, sciencePID);
		object.put(Type.SENSORPID, sensorPID);
		object.put(Type.DATASETPID, datasetPID);
		return object;
	}
	
	
}
