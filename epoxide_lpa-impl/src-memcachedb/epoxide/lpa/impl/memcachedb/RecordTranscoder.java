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

import epoxide.lpa.impl.Serializer;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

public class RecordTranscoder implements Transcoder<Object> {
	private Class<?> cls;
	
	public RecordTranscoder(Class<?> cls) {
		super();
		this.cls = cls;
	}

	@Override
	public boolean asyncDecode(CachedData arg0) {
		return false;
	}

	@Override
	public Object decode(CachedData data) {
		return Serializer.INSTANCE.deserialize(data.getData(),cls);
	}

	@Override
	public CachedData encode(Object val) {
		return new CachedData(0, Serializer.INSTANCE.serialize(val,cls), getMaxSize());
	}

	@Override
	public int getMaxSize() {
		return 20*1024*1024;
	}
}
