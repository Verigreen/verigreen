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
package com.verigreen.common.command;

import com.verigreen.common.command.AbstractTEQCommand;
import com.verigreen.common.command.params.TEQCommandParameters;

public class TEQCommandMocks {
    
    public static class TEQCommandMockDefault extends AbstractTEQCommand<TEQCommandParameters> {
        
        public static final String TYPE = "teqCommandMock";
        
        public TEQCommandMockDefault() {
            
            super(TYPE, Priority.Low);
        }
        
        @Override
        public boolean execute(TEQCommandParameters params) {
            
            return true;
        }
    }
    
    public static class TEQCommandNegativeMock extends AbstractTEQCommand<TEQCommandParameters> {
        
        public static final String TYPE = "teqCommandNegativeMock";
        
        public TEQCommandNegativeMock() {
            
            super(TYPE, Priority.Low);
        }
        
        @Override
        public boolean execute(TEQCommandParameters params) {
            
            throw new RuntimeException(TYPE);
        }
    }
    
    public static class TEQCommandHangMock extends AbstractTEQCommand<TEQCommandParameters> {
        
        public static final String TYPE = "teqCommandHangMock";
        
        public TEQCommandHangMock() {
            
            super(TYPE, Priority.Low);
        }
        
        @Override
        public boolean execute(TEQCommandParameters params) {
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
