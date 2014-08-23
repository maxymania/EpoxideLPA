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
package epoxide.lpa;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Query {
	private Map<String,Object> _having = new LinkedHashMap<String,Object>();
	public Query having(String name,Object value){
		_having.put(name, value);
		return this;
	}
	public Map<String,Object> getHaving(){
		return Collections.unmodifiableMap(_having);
	}
}
