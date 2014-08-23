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

import net.spy.memcached.MemcachedClient;
import epoxide.lpa.Database;
import epoxide.lpa.RecordSet;

public class MemcacheDatabase implements Database {
	private RecordDataPool pool;
	private MemcachedClient client;
	public MemcacheDatabase(RecordDataPool pool, MemcachedClient client) {
		this.pool = pool;
		this.client = client;
	}
	public MemcacheDatabase(MemcachedClient client) {
		this.pool = new RecordDataPool();
		this.client = client;
	}
	@Override
	public <T> RecordSet<T> getRecordSet(Class<T> clazz) {
		RecordData data = pool.get(clazz);
		return new MemcacheRecordSet<T>(clazz, data, client);
	}
}
