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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;

import indiana.edu.property.Property;

public class Filesystem{
	
	public String SENSOR_FOLDER;
	public String SCIENCE_FOLDER;
	public String DATASET_FOLDER;
	public String DOWNLOAD_FOLDER;
	
	public Filesystem(Property property) {
		this.SENSOR_FOLDER = property.property.getProperty("SENSOR_FOLDER");
		this.SCIENCE_FOLDER = property.property.getProperty("SCIENCE_FOLDER");
		this.DATASET_FOLDER = property.property.getProperty("DATASET_FOLDER");
		this.DOWNLOAD_FOLDER = property.property.getProperty("DOWNLOAD_FOLDER");
	}
	
	/*
	 * write file into the local LIDO service
	 * @param filename
	 * @param object: the content of file
	 * @param location
	 */
	public void writeFile(String fileName, String object, String location) {
		try (FileWriter write = new FileWriter(location+fileName, true)) {
			write.write(object);
            write.flush();
            write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	public void deleteFile(String fileName, String location) {
		File file = new File(location+fileName);
		file.delete();
	}
	
	/*
	 * read the metadata instance document
	 * @param fileName
	 * @param location
	 * @return the content of the metadata instance
	 */
	public JSONObject readFile(String fileName, String location) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(location+fileName))));
	    StringBuilder out = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        out.append(line);
	    }
	    JSONObject output = new JSONObject(out.toString());
		
		reader.close();
		return output;
	}
	
	/*
	 * read the metadata instance document
	 * @param path
	 * @return the content of the metadata instance
	 */
	public JSONObject readFile(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
	    StringBuilder out = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        out.append(line);
	    }
	    JSONObject output = new JSONObject(out.toString());
		
		reader.close();
		return output;
	}
	
	
	public Boolean deleteFolder(String location) {
		File dir = new File(location); 
		if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteFolder(children[i].getAbsolutePath());
                if (!success) {
                    return false;
                }
            }
        }
        System.out.println("removing file or directory : " + dir.getName());
        return dir.delete();
	}
	
	/*
	 * generate the result bundle zip file
	 * @param folderPath: the result bundle folder
	 * @param zipPath
	 */
	public void zipFolder(String folderPath, String zipPath) throws IOException {
		byte[] buffer = new byte[1024];
		 
        FileOutputStream fos = new FileOutputStream(zipPath);
        ZipOutputStream zos = new ZipOutputStream(fos);
        File dir = new File(folderPath);
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            FileInputStream fis = new FileInputStream(files[i]);
            zos.putNextEntry(new ZipEntry(files[i].getName()));           
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }

        zos.close();
	}
	
	



}
