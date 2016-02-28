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
/**
 * 
 */
package com.verigreen.common.utils;

/**
 * @author chrysler
 * 
 */
public class Pair<TFirst, TSecond> {
    
    private TFirst _first;
    private TSecond _second;
    
    public Pair(TFirst first, TSecond second) {
        
        _first = first;
        _second = second;
    }
    
    @Override
    public int hashCode() {
        
        int hashFirst = _first != null ? _first.hashCode() : 0;
        int hashSecond = _second != null ? _second.hashCode() : 0;
        
        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }
    
    @Override
    public boolean equals(Object other) {
        
        boolean ret = false;
        if (other instanceof Pair) {
            Pair<?, ?> otherPair = (Pair<?, ?>) other;
            ret =
                    ((_first == otherPair._first) || ((_first != null)
                                                       && (otherPair._first != null) && _first.equals(otherPair._first))) && ((_second == otherPair._second) || ((_second != null)
                                                                                                                                                                 && (otherPair._second != null) && _second.equals(otherPair._second)));
        }
        
        return ret;
    }
    
    @Override
    public String toString() {
        
        return String.format("(%s, %s)", _first, _second);
    }
    
    public TFirst getFirst() {
        
        return _first;
    }
    
    public void setFirst(TFirst first) {
        
        _first = first;
    }
    
    public TSecond getSecond() {
        
        return _second;
    }
    
    public void setSecond(TSecond second) {
        
        _second = second;
    }
}