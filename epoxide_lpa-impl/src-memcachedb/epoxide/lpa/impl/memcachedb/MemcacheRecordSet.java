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
package epoxide.lpa.impl.memcachedb;

import com.google.common.hash.Hashing;

import net.spy.memcached.MemcachedClient;
import epoxide.lpa.IterableIterator;
import epoxide.lpa.RecordSet;
import epoxide.lpa.impl.Empty;
import epoxide.lpa.impl.Serializer;
import epoxide.lpa.impl.StringUtil;

public class MemcacheRecordSet<T> implements RecordSet<T> {
	private Class<T> cls;
	private RecordData recdata;
	private MemcachedClient client;
	public MemcacheRecordSet(Class<T> cls, RecordData recdata,
			MemcachedClient client) {
		super();
		this.cls = cls;
		this.recdata = recdata;
		this.client = client;
	}
	private String toKey(Object id){
		return recdata.name+">"+StringUtil.hash(Hashing.murmur3_128(), Serializer.INSTANCE.kryo(), id);
	}
	@Override
	public T getEntity(Object id) {
		String k = toKey(id);
		return cls.cast(client.get(k,recdata.transcoder));
	}
	@Override
	public IterableIterator<T> getEntities() {
		return new Empty<T>();
	}
	@Override
	public void insert(T record) {
		String key = toKey(recdata.key.get(record));
		if(client.get(key)!=null)return;
		client.set(key, Integer.MAX_VALUE, record, recdata.transcoder);
	}
	@Override
	public void update(T record) {
		String key = toKey(recdata.key.get(record));
		if(client.get(key)==null)return;
		client.set(key, Integer.MAX_VALUE, record, recdata.transcoder);
	}
	@Override
	public void put(T record) {
		String key = toKey(recdata.key.get(record));
		client.set(key, Integer.MAX_VALUE, record, recdata.transcoder);
	}
	@Override
	public void delete(T record) {
		client.delete(toKey(recdata.key.get(record)));
	}
}
