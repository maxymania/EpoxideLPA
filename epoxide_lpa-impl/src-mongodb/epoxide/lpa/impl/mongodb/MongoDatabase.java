/*
   Copyright 2014 Simon Schmidt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package epoxide.lpa.impl.mongodb;

import com.mongodb.DB;
import com.mongodb.DBCollection;

import epoxide.lpa.QueryableDatabase;
import epoxide.lpa.QueryableRecordSet;
import epoxide.lpa.impl.ClassUtil;

public class MongoDatabase implements QueryableDatabase {
	private ClassConverterPool pool;
	private DB database;
	public MongoDatabase(ClassConverterPool pool, DB database) {
		super();
		this.pool = pool;
		this.database = database;
	}
	public MongoDatabase(DB database) {
		super();
		this.pool = new ClassConverterPool();
		this.database = database;
	}
	@Override
	public <T> QueryableRecordSet<T> getRecordSet(Class<T> clazz) {
		String name = ClassUtil.getClassName(clazz);
		DBCollection coll = database.getCollection(name);
		return new MongoRecordSet<T>(clazz,pool.get(clazz),coll);
	}
}
