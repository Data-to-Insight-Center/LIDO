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

import java.net.MalformedURLException;

public class QueryResponse {
    private String device;
    private String sensor;
    private String day;
    private String resultPID;
    private String status;

    public QueryResponse(String device, String sensor, String day, String resultPID, String status) throws MalformedURLException {
    	this.device = device;
        this.sensor = sensor;
        this.status = status;
        this.day = day;
        this.resultPID = resultPID;
    }

	public String getDevice() {
		return device;
	}

	public String getSensor() {
		return sensor;
	}

	public String getDay() {
		return day;
	}

	public String getResultPID() {
		return resultPID;
	}

	public String getStatus() {
		return status;
	}



}