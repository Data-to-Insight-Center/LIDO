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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;

import indiana.edu.property.Property;

public class UploadFile {
	
	private String UPLOADED_FOLDER;
	public String fileName;
	public Path path;
	
	public UploadFile(Property property) {
		this.UPLOADED_FOLDER = property.property.getProperty("UPLOADED_FOLDER");
	}
	
	public void fileUpload( MultipartFile file) {
		try {
			byte[] bytes = file.getBytes();
			this.path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
			Files.write(path, bytes);
			this.fileName = file.getOriginalFilename();
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteFile() {
		File file = new File(this.UPLOADED_FOLDER+this.fileName);
		file.delete();
	}

}
