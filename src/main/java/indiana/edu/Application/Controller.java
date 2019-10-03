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
package indiana.edu.Application;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

//import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import indiana.edu.DOA.LIDO;
import indiana.edu.data.QueryResponse;
import indiana.edu.data.UploadFile;
import indiana.edu.data.UploadFileResponse;
import indiana.edu.handle.HandleSystem;
import indiana.edu.hello.Hello;
import indiana.edu.metadata.Filesystem;
import indiana.edu.property.Property;
import net.handle.hdllib.HandleException;

@RestController
public class Controller {

    private final AtomicLong counter = new AtomicLong();
    private Property property;
    @RequestMapping("/hello")
    public Hello greeting() {
        return new Hello(counter.incrementAndGet(),
                            "Airbox Lake Service is running");
    }
   
	/*
	 * Post operation to upload the file into Unis and register the PID
	 * @param file: this is the upload raw data document
	 * @param replicate: the choice of making replications
	 * @return uploadResponse
	 */
	@PostMapping("/upload")
	public UploadFileResponse singleFileUpload(@RequestParam("file") MultipartFile file, 
			@RequestParam(required = true) Boolean replicate) throws Exception {
		
		property = new Property();
		
		if (file.isEmpty()) {
			return new UploadFileResponse("None", "No File",
	                "None", 0, "None");	
		}
		
		if (replicate) {
			System.out.println("replicate");
		}
		
		UploadFile upload = new UploadFile(property);
		upload.fileUpload(file);
		String uploadLocation = upload.path.toString();
		
		
		
		LIDO doa = new LIDO(property);
		Map<String, String> result = doa.uploadManager(uploadLocation, replicate);
		
		upload.deleteFile();
		
		
		return new UploadFileResponse(file.getOriginalFilename(), "Successfull",
	                file.getContentType(), file.getSize(), result.get("unisPIDs"));
		
	}
	
	/*
	 * Query the PID or ARO parameters to generate the result bundle, and return the PID of the result bundle to users
	 * @param pid: the PID of the Unis object or the PID of the result Bundle
	 * @param allParams: it contains three parameters: device, sensor and day
	 * @return QueryResponse
	 */
	 @GetMapping("/queryObject")
	 public QueryResponse query(@RequestParam(required = false) String pid,
			 @RequestParam Map<String,String> allParams) throws Exception {
		 property = new Property();
		 LIDO doa = new LIDO(property);	 
		 return doa.queryManager(pid, allParams);
		 	 
	  }
	 
	 
	 /*
		 * The download method for sending the result bundle to user
		 * @param pid: the PID of the result bundle
		 * @param name: unused, the name of the reuslt bundle
		 * @return the streaming data
		 */
	 @GetMapping("/download")
	 public ResponseEntity<Resource> downloadZipFile(@RequestParam(required = false) String pid, @RequestParam(required = false) String name, HttpServletRequest request) throws HandleException, Exception{
		 property = new Property();
		 HandleSystem handle = new HandleSystem(property);
		 Filesystem fileSystem = new Filesystem(property);
		 
		 Resource resource = null;
		 
		 if (pid != null) {
			 JSONObject pidKI = handle.handleResolve(pid);
			 String url = pidKI.getString("URL");
			 String fileName = url.split("=")[1];
			 resource = new PathResource(fileSystem.DOWNLOAD_FOLDER+fileName);
		 } else if (name != null) {
			 resource = new PathResource(fileSystem.DOWNLOAD_FOLDER+name);
		 }
		 
        
		 String contentType = null;
		 try {
			 contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		 } catch (IOException ex) {
		 }

		 if(contentType == null) {
			 contentType = "application/octet-stream";
		 }
		
		 return ResponseEntity.ok()
				 .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
		                	.body(resource);
		   
				
	 }
	 
	
}