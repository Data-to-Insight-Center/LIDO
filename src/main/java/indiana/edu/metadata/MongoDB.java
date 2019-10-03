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


import java.util.Map;

import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


public class MongoDB {
	
	private String dbName = "Cordra";
	private DB db;

	private Mongo mongo;
	
	public MongoDB() {
		this.mongo = new Mongo( "127.0.0.1");
        this.db = mongo.getDB( dbName );
	}

	
	/*
	 * insert the metadata instance
	 */
	public ObjectId insertDocument(String collection, JSONObject input) {
		ObjectId id = null;
		try {
			Object temp = com.mongodb.util.JSON.parse(input.toString());
			DBObject document = (DBObject) temp;
			db.getCollection(collection).insert(document);
			id = (ObjectId) document.get("_id");
		}  catch (MongoException e) {
			e.printStackTrace();
		}
		
		return id;
	}
	
	/*
	 * update the metadata instance
	 */
	public void updateDocument(String collection, ObjectId id, JSONObject input) {
		try {
			Object temp = com.mongodb.util.JSON.parse(input.toString());
			DBObject document = (DBObject) temp; 
			
			BasicDBObject newDocument = new BasicDBObject();
			newDocument.append("$set", document);
			
			BasicDBObject searchQuery = new BasicDBObject().append("_id", id);
			db.getCollection(collection).update(searchQuery, newDocument);
			
		}  catch (MongoException e) {
			e.printStackTrace();

		}
	}
	
	/*
	 * delete the metadata instance
	 */
	public void deleteDocument(String collection, ObjectId id) {
		try {
			db.getCollection(collection).remove(new BasicDBObject().append("_id", id));
			
		}  catch (MongoException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * find the metadata instance
	 */
	public JSONObject findDocument(String collection, String id, Map<String, String> pair) {
		 BasicDBObject query = new BasicDBObject();
		 
		 if (!id.equals("")) {
			 query.put("id", id);
		 } else {
			 for (String key : pair.keySet()) {
				 query.put(key, pair.get(key));
			 }
		 }

		 DBCursor result =  (db.getCollection(collection)).find(query);
		 JSONObject output = new JSONObject(result.next().toString());
		 return output;
		
	}
	
	public void close() {
		this.mongo.close();
	}


	

}
