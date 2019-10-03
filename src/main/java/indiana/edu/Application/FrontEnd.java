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
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


import indiana.edu.DOA.LIDO;
import indiana.edu.data.QueryResponse;
import indiana.edu.data.UploadFile;
import indiana.edu.dtr.Type;
import indiana.edu.property.Property;

@Controller
public class FrontEnd {
	
	private Property property;
	
	/*
	 * jumping to Home page
	 * @param
	 * @param
	 * @return
	 */
	@GetMapping("/")
	public String home()  {
        return "home";
    }
	
	/*
	 * jumping to upload page
	 * @param
	 * @param
	 * @return
	 */
	@GetMapping("/upload")
	public String uploadPage() {
		return "upload";
	}
	
	/*
	 * jumping to query page
	 * @param
	 * @param
	 * @return
	 */
	@GetMapping("/query")
	public String queryPage() {
		return "query";
	}
	
	/*
	 * Query page, using PID of the Unis object or PID of the result bunlde to query/generate the abstract object
	 * @param pid: pid of unis object or the reuslt bundle
	 * @param device: the device name
	 * @param sensor: the sensor type
	 * @param day: the day of sensor reading
	 * @return the query reuslt including the device, sensor, day, status and PID result bundle
	 */
	@GetMapping("/queryPage")
	public ModelAndView query(@RequestParam(required = false) String pid,
			 @RequestParam(required = false) String device,
			 @RequestParam(required = false) String sensor,
			 @RequestParam(required = false) String day) throws Exception {
		
		Map<String, String> allParams = new HashMap<String, String>();
		allParams.put(Type.DEVICEMODEL, device);
		allParams.put(Type.SENSORTYPE, sensor);
		allParams.put(Type.STARTDATE, day);
		
		property = new Property();
		LIDO doa = new LIDO(property);
		QueryResponse result = doa.queryManager(pid, allParams);
		ModelAndView mav = new ModelAndView("result");
		mav.addObject("device", result.getDevice());
		mav.addObject("sensor", result.getSensor());
		mav.addObject("day", result.getDay());
		mav.addObject("queryStatus", result.getStatus());
		mav.addObject("resultPID", result.getResultPID());
		return mav;
	}
	
	
	/*
	 * The result page/upload page showing the query or uploading result
	 * @param file: the raw data document
	 * @param: replicate: the choice of making replication
	 * @return the result of uploading raw data document, including the PID of the unis object
	 */
	@PostMapping("/result")
	public ModelAndView upload(@RequestParam("file") MultipartFile file, 
			@RequestParam(required = true) Boolean replicate) throws Exception  {
			property = new Property();
			ModelAndView mav = new ModelAndView("result");
			
			if (file.isEmpty()) {
				mav.addObject("fileName", "None");
				mav.addObject("status", "Fail");
				mav.addObject("type", "None");
				mav.addObject("size", "None");
				mav.addObject("unisPID", "None");
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
			
			mav.addObject("name", file.getOriginalFilename());
			mav.addObject("status", "Success");
			mav.addObject("type", file.getContentType());
			mav.addObject("size", file.getSize());
			mav.addObject("unisPIDs", result.get("unisPIDs"));
			
			return mav;
    }
	
}
