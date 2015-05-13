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
package com.verigreen.main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {

	public static void main(String[] args) throws NoSuchAlgorithmException {

		 if (args == null || args.length == 0){
			 System.out.println("You need to enter password.");
		 }
		 else if (args[0].equals("help")){
			 System.out.println("run command 'java -jar appname.jar password'");
		 }
		 else{
			 convertPassword(args[0]);
		 }
		
	}

	private static void convertPassword(String password) throws NoSuchAlgorithmException {

	    MessageDigest md = MessageDigest.getInstance("SHA-1");
	    md.update(password.getBytes());

	    byte byteData[] = md.digest();

	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < byteData.length; i++)
	        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

	    System.out.println("Hash Password: " + sb.toString());
	}

}
