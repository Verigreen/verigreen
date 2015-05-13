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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;

import com.verigreen.common.command.params.TEQCommandParameters;
import com.verigreen.common.concurrency.ExecutorServiceFactory;
import com.verigreen.common.concurrency.timeboundedexecuter.KeepWaitingPolicy;
import com.verigreen.common.concurrency.timeboundedexecuter.TimeBoundedExecuter;
import com.verigreen.common.utils.Action;

public class TEQCommandExecuter {
    
    @Autowired
    private TimeBoundedExecuter _timeBoundedExecuter;
    private int _timeoutMillis;
    
    private TEQCommandExecuter() {}
    
    public <TCommandParameters extends TEQCommandParameters> void execute(
            CommandPack<TCommandParameters> commandPack,
            KeepWaitingPolicy<?> keepWaitingPolicy) {
        
        _timeBoundedExecuter.perform(
                new RunnableExecuter<TCommandParameters>(
                        commandPack.getCommand(),
                        commandPack.getParameters()),
                _timeoutMillis,
                keepWaitingPolicy);
    }
    
    public <TCommandParameters extends TEQCommandParameters> void execute(
            CommandPack<TCommandParameters> commandPack) {
        
        _timeBoundedExecuter.perform(
                new RunnableExecuter<TCommandParameters>(
                        commandPack.getCommand(),
                        commandPack.getParameters()),
                _timeoutMillis);
    }
    
    public <TCommandParameters extends TEQCommandParameters> void execute(
            List<CommandPack<TCommandParameters>> commandPacks) throws InterruptedException {
        
        List<Callable<Void>> callables = createCallables(commandPacks);
        List<Future<Void>> futures =
                ExecutorServiceFactory.getCachedThreadPoolExecutor().invokeAll(
                        callables,
                        _timeoutMillis,
                        TimeUnit.MILLISECONDS);
        getResults(commandPacks, futures);
    }
    
    public int getTimeoutMillis() {
        
        return _timeoutMillis;
    }
    
    public void setTimeout(int timeout) {
        
        _timeoutMillis = timeout;
    }
    
    private <TCommandParameters extends TEQCommandParameters> List<Callable<Void>> createCallables(
            List<CommandPack<TCommandParameters>> commandPacks) {
        
        List<Callable<Void>> callables = new ArrayList<>();
        for (final CommandPack<TCommandParameters> pack : commandPacks) {
            callables.add(new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    
                    pack.getCommand().execute(pack.getParameters());
                    
                    return null;
                }
            });
        }
        
        return callables;
    }
    
    private <TCommandParameters extends TEQCommandParameters> void getResults(
            List<CommandPack<TCommandParameters>> commandPacks,
            List<Future<Void>> futures) throws InterruptedException {
        
        for (int i = 0; i < futures.size(); ++i) {
            // futures and commandpacks have the same order - invokeAll guarantees that
            try {
                futures.get(i).get(0, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                commandPacks.get(i).setError(CommandResult.TIMEOUT);
            } catch (ExecutionException e) {
                commandPacks.get(i).setError(e.getMessage());
            }
        }
    }
    
    private static class RunnableExecuter<TCommandParameters extends TEQCommandParameters>
            implements Action<Void> {
        
        private final TEQCommand<TCommandParameters> _command;
        private final TCommandParameters _commandParameters;
        
        public RunnableExecuter(
                TEQCommand<TCommandParameters> command,
                TCommandParameters commandParameters) {
            
            _command = command;
            _commandParameters = commandParameters;
        }
        
        @Override
        public Void action() {
            
            _command.execute(_commandParameters);
            
            return null;
        }
        
        @Override
        public String toString() {
            
            return MessageFormat.format(
                    "RunnableExecuter [_command={0}, _commandParameters={1}]",
                    _command,
                    _commandParameters);
        }
    }
}
