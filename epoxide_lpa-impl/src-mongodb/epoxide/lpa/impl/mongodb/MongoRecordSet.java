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

import java.util.Map.Entry;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import epoxide.lpa.IterableIterator;
import epoxide.lpa.Query;
import epoxide.lpa.QueryableRecordSet;

public class MongoRecordSet<T> implements QueryableRecordSet<T> {
	private Class<T> cls;
	private ClassConverter conv;
	private DBCollection coll;
	
	public MongoRecordSet(Class<T> cls, ClassConverter conv, DBCollection coll) {
		super();
		this.cls = cls;
		this.conv = conv;
		this.coll = coll;
	}

	@Override
	public T getEntity(Object id) {
		DBCursor cursor = coll.find(new BasicDBObject("_id",id));
		if(cursor.hasNext()){
			DBObject result = cursor.next();
			cursor.close();
			return cls.cast(conv.decode(result));
		}
		cursor.close();
		return null;
	}

	@Override
	public IterableIterator<T> getEntities() {
		DBCursor cursor = coll.find();
		return new MongoIterator<T>(cls,conv,cursor);
	}
	@Override
	public IterableIterator<T> getEntities(Query query) {
		BasicDBObject nq = new BasicDBObject();
		for(Entry<String, Object> e:query.getHaving().entrySet())
			nq.append(
					conv.translateFieldName(e.getKey()),
					Natives.encode(e.getValue())
			);
		DBCursor cursor = coll.find(nq);
		return new MongoIterator<T>(cls,conv,cursor);
	}

	@Override
	public void insert(T record) {
		coll.insert(conv.encode(record));
	}

	@Override
	public void update(T record) {
		coll.save(conv.encode(record));
	}

	@Override
	public void put(T record) {
		coll.save(conv.encode(record));
	}

	@Override
	public void delete(T record) {
		coll.remove(new BasicDBObject("_id",conv.id(record)));
	}
}
