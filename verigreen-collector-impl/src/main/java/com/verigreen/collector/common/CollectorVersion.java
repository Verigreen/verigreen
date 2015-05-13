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
package com.verigreen.collector.common;

public class CollectorVersion {
	private static String _collectorVersion;

	public String get_collectorVersion() {
		return _collectorVersion;
	}

	public void set_collectorVersion(String _collectorVersion) {
		CollectorVersion._collectorVersion = _collectorVersion;
	}
	
	@Override
    public String toString() {
        
        return String.format(
                "Collector [_collectorVersion=%s]",
                _collectorVersion);
    }
}

