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

public class UploadFileResponse {
    private String fileName;
    private String fileType;
    private String status;
    private long size;
    private String unisPIDs;


    public UploadFileResponse(String fileName, String status, String fileType, long size, String unisPIDs) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.status = status;
        this.size = size;
    }

	public String getFileName() {
		return fileName;
	}


	public String getFileType() {
		return fileType;
	}


	public String getStatus() {
		return status;
	}



	public long getSize() {
		return size;
	}

	public String getUnisPIDs() {
		return unisPIDs;
	}
	

	

}