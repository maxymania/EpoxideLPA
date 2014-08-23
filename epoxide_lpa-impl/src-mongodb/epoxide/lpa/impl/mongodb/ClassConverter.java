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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.bson.BSONObject;
import org.bson.types.Binary;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.mongodb.BasicDBObject;

import epoxide.lpa.annotation.Id;
import epoxide.lpa.impl.ClassUtil;
import epoxide.lpa.impl.Serializer;

public class ClassConverter {
	private ObjectInstantiator<?> oi;
	private FieldAccess fa;
	private FieldInfo[] infos;
	private FieldInfo idfield;
	private String idfieldname = "";
	private static class FieldInfo{
		public String key;
		public int num;
		public Class<?> type;
		public boolean isNative;
		public boolean isBytes;
	}
	public ClassConverter(Class<?> cls){
		{
			Objenesis object = new ObjenesisStd();
			oi = object.getInstantiatorOf(cls);
		}
		fa = FieldAccess.get(cls);
		List<FieldInfo> info_list = new ArrayList<FieldInfo>();
		for(Field f:cls.getFields()){
			{
				int mod = f.getModifiers();
				if(
						Modifier.isStatic(mod)
						||!Modifier.isPublic(mod)
				)continue;
			}
			FieldInfo info = new FieldInfo();
			info.type = f.getType();
			Class<?> filtered = ClassUtil.convert(info.type);
			info.key = f.getName();
			info.num = fa.getIndex(info.key);
			info.isNative=Natives.NATIVES.contains(filtered);
			info.isBytes = byte[].class.equals(info.type);
			if(f.getAnnotation(Id.class)!=null){
				idfieldname = info.key;
				info.key = "_id";
				idfield = info;
			}
			info_list.add(info);
		}
		infos = info_list.toArray(new FieldInfo[info_list.size()]);
	}
	public BasicDBObject encode(Object record){
		BasicDBObject dest = new BasicDBObject();
		for(FieldInfo info:infos){
			Object val = fa.get(record, info.num);
			if(info.isBytes){
				if(val!=null)
					val = new Binary((byte[])val);
			}else if(!info.isNative){
				val = new Binary(Serializer.INSTANCE.serialize(val));
			}
			dest.append(info.key, val);
		}
		return dest;
	}
	public Object decode(BSONObject data){
		Object record = oi.newInstance();
		for(FieldInfo info:infos){
			Object val = data.get(info.key);
			if(info.isBytes){
				if(val!=null)
					val = ((Binary)val).getData();
			}else if(info.isNative){
				val = ClassUtil.handleNull(val, info.type);
			}else{
				if(val!=null)
					val = Serializer.INSTANCE.deserialize(((Binary)val).getData());
			}
			fa.set(record, info.num, val);
		}
		return record;
	}
	public Object id(Object record){
		Object val = fa.get(record,idfield.num);
		if(idfield.isBytes){
			if(val!=null)
				val = new Binary((byte[])val);
		}else if(!idfield.isNative){
			val = new Binary(Serializer.INSTANCE.serialize(val));
		}
		return val;
	}
	public String translateFieldName(String name){
		if(idfieldname.equals(name))
			return "_id";
		return name;
	}
}
