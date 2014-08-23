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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bson.types.Binary;

import epoxide.lpa.impl.Serializer;

public class Natives {
	public static final Set<Class<?> > NATIVES;
	static{
		HashSet<Class<?> > nats = new HashSet<Class<?> >();
		nats.add(Boolean.class);
		nats.add(Byte.class);
		nats.add(Short.class);
		nats.add(Integer.class);
		nats.add(Long.class);
		nats.add(Float.class);
		nats.add(Double.class);
		nats.add(Character.class);
		nats.add(BigInteger.class);
		nats.add(BigDecimal.class);
		nats.add(String.class);
		NATIVES = Collections.unmodifiableSet(nats);
	}
	public static Object encode(Object data){
		if(data==null)return null;
		if(!NATIVES.contains(data.getClass())){
			if(data instanceof byte[])
				data = new Binary((byte[])data);
			else
				data = new Binary(Serializer.INSTANCE.serialize(data));
		}
		return data;
	}
}
