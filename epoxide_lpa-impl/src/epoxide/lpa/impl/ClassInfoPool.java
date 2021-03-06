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

import java.util.HashMap;
import java.util.Map;

public abstract class ClassInfoPool<T> {
	private Map<String,T> map = new HashMap<String,T>();
	public synchronized T get(Class<?> cls){
		String name = cls.getSimpleName();
		if(map.containsKey(name))
			return map.get(name);
		T t = create(cls);
		map.put(name, t);
		return t;
	}
	protected abstract T create(Class<?> cls);
}
