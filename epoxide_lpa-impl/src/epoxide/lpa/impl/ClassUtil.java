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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassUtil {
	public static final Map<Class<?>,Class<?> > MAPPING;
	public static final Map<Class<?>,Object> DEFAULTS;
	public static final Pattern CLSNAME = Pattern.compile("[^\\.]+$");
	static{
		Map<Class<?>,Class<?> > my_mapping = new HashMap<Class<?>,Class<?> >();
		my_mapping.put(boolean.class, Boolean.class);
		my_mapping.put(byte.class, Byte.class);
		my_mapping.put(short.class, Short.class);
		my_mapping.put(int.class, Integer.class);
		my_mapping.put(long.class, Long.class);
		my_mapping.put(float.class, Float.class);
		my_mapping.put(double.class, Double.class);
		my_mapping.put(char.class, Character.class);
		MAPPING = Collections.unmodifiableMap(my_mapping);
		Map<Class<?>,Object> my_defaults = new HashMap<Class<?>,Object>();
		my_defaults.put(boolean.class, false);
		my_defaults.put(byte.class, (byte)0);
		my_defaults.put(short.class, (short)0);
		my_defaults.put(int.class, (int)0);
		my_defaults.put(long.class, (long)0);
		my_defaults.put(float.class, (float)0);
		my_defaults.put(double.class, (double)0);
		my_defaults.put(char.class, (char)0);
		DEFAULTS = Collections.unmodifiableMap(my_defaults);
	}
	public static String getClassName(Class<?> cls){
		Matcher mat = CLSNAME.matcher(cls.getSimpleName());
		if(mat.find())
			return mat.group();
		return cls.getSimpleName();
	}
	public static Class<?> convert(Class<?> cls){
		Class<?> nc = MAPPING.get(cls);
		return nc!=null?nc:cls;
	}
	public static boolean primitive(Class<?> cls){
		return MAPPING.get(cls)!=null;
	}
	public static Object handleNull(Object obj,Class<?> cls){
		if(obj!=null)
			return obj;
		return DEFAULTS.get(cls);
	}
}
