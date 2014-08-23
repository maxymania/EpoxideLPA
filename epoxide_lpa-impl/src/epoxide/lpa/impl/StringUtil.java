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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.hash.Funnels;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

public class StringUtil {
	private static final String HEX = "0123456789abcdef";
	public static String hex(byte[] data){
		char[] out = new char[data.length*2];
		for(int i=0,n=data.length;i<n;++i){
			out[i*2]=HEX.charAt(data[i]&0xf);
			out[(i*2)+1]=HEX.charAt((data[i]>>4)&0xf);
		}
		return new String(out);
	}
	public static String hash(HashFunction hf, Kryo kryo, Object obj){
		Hasher ho = hf.newHasher();
		Output out = new Output(Funnels.asOutputStream(ho));
		kryo.writeClassAndObject(out, obj);
		out.close();
		return hex(ho.hash().asBytes());
	}
}
