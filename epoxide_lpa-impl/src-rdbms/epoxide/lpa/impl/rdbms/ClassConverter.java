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
package epoxide.lpa.impl.rdbms;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import com.esotericsoftware.reflectasm.FieldAccess;

import epoxide.lpa.annotation.Id;
import epoxide.lpa.annotation.Large;
import epoxide.lpa.impl.ClassUtil;
import epoxide.lpa.impl.Serializer;

public class ClassConverter {
	private static final int
	TBYTE  = 0,
	TSHORT = 1,
	TINT = 2,
	TLONG = 3,
	TFLOAT = 4,
	TDOUBLE = 5,
	TCHARACTER = 6,
	TBOOLEAN = 7,
	TSTRING = 8,
	TCHARARRAY = 9,
	TBINARY = 10,
	TSERIALIZE = 11;
	private static final Map<Class<?>,Integer> ITYPES;
	private static final Map<Class<?>,SqlDialect.Type> STYPES;
	static{
		Map<Class<?>,Integer> itypes = new HashMap<Class<?>,Integer>();
		itypes.put(Byte.class, TBYTE);
		itypes.put(Short.class, TSHORT);
		itypes.put(Integer.class, TINT);
		itypes.put(Long.class, TLONG);
		itypes.put(Float.class, TFLOAT);
		itypes.put(Double.class, TDOUBLE);
		itypes.put(Character.class, TCHARACTER);
		itypes.put(Boolean.class, TBOOLEAN);
		itypes.put(String.class, TSTRING);
		itypes.put(char[].class, TCHARARRAY);
		itypes.put(byte[].class, TBINARY);
		ITYPES = Collections.unmodifiableMap(itypes);
		Map<Class<?>,SqlDialect.Type> stypes = new HashMap<Class<?>,SqlDialect.Type>();
		stypes.put(Byte.class, SqlDialect.Type.INTSHORT);
		stypes.put(Short.class, SqlDialect.Type.INTSHORT);
		stypes.put(Integer.class, SqlDialect.Type.INT);
		stypes.put(Long.class, SqlDialect.Type.INTLONG);
		stypes.put(Float.class, SqlDialect.Type.FLOAT);
		stypes.put(Double.class, SqlDialect.Type.DOUBLE);
		stypes.put(Character.class, SqlDialect.Type.INT);
		stypes.put(Boolean.class, SqlDialect.Type.BOOLEAN);
		stypes.put(String.class, SqlDialect.Type.STRING);
		stypes.put(char[].class, SqlDialect.Type.STRING);
		stypes.put(byte[].class, SqlDialect.Type.BINARY);
		STYPES = Collections.unmodifiableMap(stypes);
	}
	private static int getTypeNum(Class<?> cls){
		Integer i = ITYPES.get(cls);
		return i==null?TSERIALIZE:i;
	}
	private ObjectInstantiator<?> oi;
	private FieldAccess fa;
	private FieldInfo[] infos;
	private String name;
	private FieldInfo keyfield;
	private int[] query;
	private int[] update;
	private static class FieldInfo{
		public String key;
		public int num;
		public Class<?> type;
		public int typenum;
		public SqlDialect.Type sqlType;
		public boolean isLarge;
		public boolean notNull;
		public boolean isPrimaryKey;
		@Override
		public String toString() {
			return "FieldInfo [key=" + key + ", num=" + num + ", type=" + type
					+ ", typenum=" + typenum + ", sqlType=" + sqlType
					+ ", isLarge=" + isLarge + ", notNull=" + notNull
					+ ", isPrimaryKey=" + isPrimaryKey + "]";
		}
	}
	public ClassConverter(Class<?> cls){
		{
			Objenesis object = new ObjenesisStd();
			oi = object.getInstantiatorOf(cls);
		}
		name = ClassUtil.getClassName(cls);
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
			info.isLarge = f.getAnnotation(Large.class)!=null;
			info.typenum = getTypeNum(filtered);
			info.sqlType = STYPES.get(filtered);
			if(info.sqlType==null)
				info.sqlType=SqlDialect.Type.BINARY;
			if(info.isLarge)
				switch(info.sqlType){
				case BINARY:
					info.sqlType=SqlDialect.Type.BLOB;break;
				case STRING:
					info.sqlType=SqlDialect.Type.CLOB;
				}
			info.notNull = ClassUtil.primitive(info.type);
			info.isPrimaryKey = f.getAnnotation(Id.class)!=null;
			if(info.isPrimaryKey)keyfield=info;
			info_list.add(info);
		}
		infos = info_list.toArray(new FieldInfo[info_list.size()]);
		query = new int[infos.length];
		update = new int[infos.length];
		for(int i=0,n=infos.length;i<n;++i)
			query[i]=i+1;
		int c=0;
		for(int i=0,n=infos.length;i<n;++i)
			if(!infos[i].isPrimaryKey)
				update[c++]=i+1;
		for(int i=0,n=infos.length;i<n;++i)
			if(infos[i].isPrimaryKey)
				update[c++]=i+1;
	}
	public void encode(Object record,PreparedStatement prep) throws SQLException{
		encode(record,prep,this.query);
	}
	public void encodeUpdate(Object record,PreparedStatement prep) throws SQLException{
		encode(record,prep,this.update);
	}
	private void encode(Object record,PreparedStatement prep,int[] numbers) throws SQLException{
		for(int i=0,n=infos.length;i<n;++i){
			FieldInfo info = infos[i];
			int index = numbers[i];
			Object val = fa.get(record, info.num);
			set(prep,index,val,info.typenum,info.isLarge);
		}
	}
	public Object decode(ResultSet data) throws SQLException{
		return decode(data,query);
	}
	private Object decode(ResultSet data,int[] numbers) throws SQLException{
		Object record = oi.newInstance();
		for(int i=0,n=infos.length;i<n;++i){
			FieldInfo info = infos[i];
			int index = numbers[i];
			Object val = get(data,index,info.typenum,info.isLarge,info.notNull);
			fa.set(record, info.num, val);
		}
		return record;
	}
	public String createTable(SqlDialect dialect){
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ").append(name).append("(");
		boolean first = true;
		for(FieldInfo info:infos){
			if(first)
				first=false;
			else
				sb.append(", ");
			sb.append(info.key).append(" ").append(dialect.sqlType(info.sqlType));
			if(info.notNull)
				sb.append(" NOT NULL");
			if(info.isPrimaryKey)
				sb.append(" PRIMARY KEY");
		}
		return sb.append(");").toString();
	}
	public String createSelect(SqlDialect dialect,List<String> byFields){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		boolean first = true;
		for(FieldInfo info:infos){
			if(first)
				first=false;
			else
				sb.append(", ");
			sb.append(info.key);
		}
		sb.append(" FROM ").append(name);
		if(!byFields.isEmpty())
			sb.append(" WHERE ");
		first = true;
		for(String field:byFields){
			if(first)
				first=false;
			else
				sb.append(", ");
			sb.append(field).append("=?");
		}
		return sb.append(";").toString();
	}
	public String createSelectByKey(SqlDialect dialect){
		return createSelect(dialect,Arrays.asList(keyfield.key));
	}
	public String createInsert(SqlDialect dialect){
		StringBuilder sb = new StringBuilder();
		StringBuilder quest = new StringBuilder();
		sb.append("INSERT INTO ").append(name).append("(");
		boolean first = true;
		for(FieldInfo info:infos){
			if(first)
				first=false;
			else{
				sb.append(", ");
				quest.append(", ");
			}
			sb.append(info.key);
			quest.append("?");
		}
		sb.append(") VALUES (").append(quest).append(");");
		return sb.toString();
	}
	public String createUpdate(SqlDialect dialect){
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(name).append(" SET ");
		boolean first = true;
		for(FieldInfo info:infos){
			if(info.isPrimaryKey)continue;
			if(first)
				first=false;
			else
				sb.append(", ");
			sb.append(info.key).append("=?");
		}
		sb.append(" WHERE ");
		first = true;
		for(FieldInfo info:infos){
			if(!info.isPrimaryKey)continue;
			if(first)
				first=false;
			else
				sb.append(", ");
			sb.append(info.key).append("=?");
		}
		return sb.append(";").toString();
	}
	public String createDelete(SqlDialect dialect){
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(name);
		boolean first = true;
		sb.append(" WHERE ");
		for(FieldInfo info:infos){
			if(!info.isPrimaryKey)continue;
			if(first)
				first=false;
			else
				sb.append(", ");
			sb.append(info.key).append("=?");
		}
		return sb.append(";").toString();
	}
	
	private Object get(ResultSet data, int index, int typenum,
			boolean isLarge, boolean notNull) throws SQLException {
		boolean canNull = notNull;
		Object result = null;
		switch(typenum){
		case TBYTE:
			result = data.getByte(index);
			if(canNull&&data.wasNull())result = null;
			break;
		case TSHORT:
			result = data.getShort(index);
			if(canNull&&data.wasNull())result = null;
			break;
		case TINT:
			result = data.getInt(index);
			if(canNull&&data.wasNull())result = null;
			break;
		case TLONG:
			result = data.getLong(index);
			if(canNull&&data.wasNull())result = null;
			break;
		case TFLOAT:
			result = data.getFloat(index);
			if(canNull&&data.wasNull())result = null;
			break;
		case TDOUBLE:
			result = data.getDouble(index);
			if(canNull&&data.wasNull())result = null;
			break;
		case TBOOLEAN:
			result = data.getBoolean(index);
			if(canNull&&data.wasNull())result = null;
			break;
		case TSTRING:
			if(isLarge){
				Clob clob = data.getClob(index);
				if(clob!=null){
					result = clob.getSubString(0, (int)(Math.min(Integer.MAX_VALUE, clob.length())));
					clob.free();
				}
			}else
				result = data.getString(index);
			break;
		case TCHARARRAY:
			if(isLarge){
				Clob clob = data.getClob(index);
				if(clob!=null){
					result = clob.getSubString(0, (int)(Math.min(Integer.MAX_VALUE, clob.length()))).toCharArray();
					clob.free();
				}
			}else
				result = data.getString(index).toCharArray();
			break;
		case TBINARY:
			if(isLarge){
				Blob blob = data.getBlob(index);
				if(blob!=null){
					result = blob.getBytes(0, (int)(Math.min(Integer.MAX_VALUE, blob.length())));
					blob.free();
				}
			}else
				result = data.getBytes(index);
			break;
		case TSERIALIZE:
			{
				byte[] content;
				if(isLarge){
					Blob blob = data.getBlob(index);
					if(blob!=null){
						content = blob.getBytes(0, (int)(Math.min(Integer.MAX_VALUE, blob.length())));
						blob.free();
					}else
						content=null;
				}else
					content = data.getBytes(index);
				if(content!=null)
					result = Serializer.INSTANCE.deserialize(content);
			}
		}
		return result;
	}
	public void setId(PreparedStatement prep,Object val) throws SQLException{
		set(prep,1,val,keyfield.typenum,keyfield.isLarge);
	}
	public void setIdFromObject(PreparedStatement prep,Object record) throws SQLException{
		Object val = fa.get(record, keyfield.num);
		set(prep,1,val,keyfield.typenum,keyfield.isLarge);
	}
	private void set(PreparedStatement prep, int index, Object val, int typenum, boolean isLarge) throws SQLException {
		if(val==null && typenum!=TSERIALIZE){
			switch(typenum){
			case TBYTE:
			case TSHORT:
				prep.setNull(index, Types.SMALLINT);break;
			case TCHARACTER:
			case TINT:
				prep.setNull(index, Types.INTEGER);break;
			case TLONG:
				prep.setNull(index, Types.BIGINT);break;
			case TFLOAT:
				prep.setNull(index, Types.FLOAT);break;
			case TDOUBLE:
				prep.setNull(index, Types.DOUBLE);break;
			case TBOOLEAN:
				prep.setNull(index, Types.BOOLEAN);break;
			case TSTRING:
			case TCHARARRAY:
				prep.setNull(index, isLarge?Types.CLOB:Types.VARCHAR);break;
			case TBINARY:
			case TSERIALIZE:
			default:
				prep.setNull(index, isLarge?Types.BLOB:Types.VARBINARY);break;
			}
			return;
		}
		switch(typenum){
		case TBYTE:
			prep.setByte(index, (Byte)val);break;
		case TSHORT:
			prep.setShort(index, (Short)val);break;
		case TINT:
			prep.setInt(index, (Integer)val);break;
		case TLONG:
			prep.setLong(index, (Long)val);break;
		case TFLOAT:
			prep.setFloat(index, (Float)val);break;
		case TDOUBLE:
			prep.setDouble(index, (Double)val);break;
		case TBOOLEAN:
			prep.setBoolean(index, (Boolean)val);break;
		case TSTRING:
			String str = (String)val;
			if(isLarge)
				prep.setClob(index, new StringReader(str), str.length());
			else
				prep.setString(index, str);break;
		case TCHARARRAY:
			char[] chrr = (char[])val;
			if(isLarge)
				prep.setClob(index, new CharArrayReader(chrr), chrr.length);
			else
				prep.setString(index, new String(chrr));break;
		case TBINARY:
			byte[] data = (byte[])val;
			if(isLarge)
				prep.setBlob(index, new ByteArrayInputStream(data),data.length);
			else
				prep.setBytes(index, data);
		case TSERIALIZE:
			data = Serializer.INSTANCE.serialize(val);
			if(isLarge)
				prep.setBlob(index, new ByteArrayInputStream(data),data.length);
			else
				prep.setBytes(index, data);
		}
	}
}
