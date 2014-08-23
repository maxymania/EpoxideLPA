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
package epoxide.lpa.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Serializer {
	private ThreadLocal<Kryo> kryopool = new ThreadLocal<Kryo>(){
		@Override
		protected Kryo initialValue() {
			return new Kryo();
		}
	};
	public static final Serializer INSTANCE = new Serializer();
	public byte[] serialize(Object obj){
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
		Output destput = new Output(dest);
		kryopool.get().writeClassAndObject(destput, obj);
		destput.close();
		return dest.toByteArray();
	}
	public Object deserialize(byte[] data){
		ByteArrayInputStream src = new ByteArrayInputStream(data);
		Input srcput = new Input(src);
		return kryopool.get().readClassAndObject(srcput);
	}
	
	public byte[] serialize(Object obj,Class<?> cls){
		if(!obj.getClass().equals(cls))throw new IllegalArgumentException(obj.getClass()+"!="+cls);
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
		Output destput = new Output(dest);
		kryopool.get().writeObject(destput, obj);
		destput.close();
		return dest.toByteArray();
	}
	public Object deserialize(byte[] data,Class<?> cls){
		ByteArrayInputStream src = new ByteArrayInputStream(data);
		Input srcput = new Input(src);
		return kryopool.get().readObject(srcput, cls);
	}
	public Kryo kryo() { return kryopool.get(); }
}
