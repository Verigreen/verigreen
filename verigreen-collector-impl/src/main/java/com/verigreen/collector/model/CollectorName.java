/*******************************************************************************
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.verigreen.collector.model;


public class CollectorName {
	public static String _collector;

	public String get_collector() {
		return _collector;
	}

	public void set_collector(String _collector) {
		CollectorName._collector = _collector;
	}
	
	@Override
    public String toString() {
        
        return String.format(
                "Collector [\n\t_collector=%s]",
                _collector);
    }
}
