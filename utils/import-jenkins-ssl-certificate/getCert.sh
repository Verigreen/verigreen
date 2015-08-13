#!/bin/bash
#*******************************************************************************
# Copyright 2015 Hewlett-Packard Development Company, L.P.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
###############################################################################
# This script downloads the SSL security certificate from the server specified
# in the ADDRESS and PORT variables, to /tmp.
# Then, it imports the certificate to the java keystore of the current
# JAVA_HOME version
###############################################################################
ADDRESS=$1
PORT=$2

echo -n | openssl s_client -connect $ADDRESS:$PORT | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /tmp/$ADDRESS.$PORT.cert
$JAVA_HOME/bin/keytool -import -alias $ADDRESS-$PORT -keystore $JAVA_HOME/jre/lib/security/cacerts -file /tmp/$ADDRESS.$PORT.cert
