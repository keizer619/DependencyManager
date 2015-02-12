/**
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/


package org.wso2.carbon.build.tools.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.build.tools.Constants;

public class PatternMatch {

    private static final Log logger = LogFactory.getLog(PatternMatch.class);
	BufferedReader buffReader = null;

	String checkVariable=null;
	String sCurrentLine=null;
	String checkLine=null;
	String max="0.0";

	int indexCheckLine=0;
	int indexVersion=0;

	public String getMAtchDetails(String sGroupId,String sVersion,String completeUrl,String repository){ 
		try {
			buffReader = new BufferedReader(new FileReader(Constants.READ_FILE_PATH));

			if(repository.equalsIgnoreCase("org/wso2")){
				int urlIndex=completeUrl.length();
				while ((sCurrentLine = buffReader.readLine()) != null) {
					String text = sCurrentLine;
					if (text.contains("<a href=\""+completeUrl+"/")){
						int index=text.indexOf('"');
						checkLine=text.substring(index+urlIndex+2);		
						checkLine=checkLine.split("/")[0];
						char c=checkLine.charAt(0);
						if(Character.isDigit(c)){
							try{
							checkVariable=checkLatestVersion(checkLine, max);
							}catch(Exception e){
								
							}
						}				
					}
					
				}
				//Check whether the version exists
				HttpURLConnection httpUrlConn;
				try{
					httpUrlConn = (HttpURLConnection) new URL(completeUrl+"/"+sVersion).openConnection();
					httpUrlConn.setRequestMethod("HEAD");
					httpUrlConn.setConnectTimeout(30000);
					httpUrlConn.setReadTimeout(30000);

					if(httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK==true){
				    	VersionManager versionManager = new VersionManager();
				    	versionManager.setValid();
					}
				}catch (Exception ex) {
                    logger.error("Exception occurred : " + ex.getMessage());
				}
		
		}else{
			while ((sCurrentLine = buffReader.readLine()) != null) {
					String text = sCurrentLine;
					if (sCurrentLine.contains("class=\"vbtn")){
						checkLine = text.substring(text.lastIndexOf("\"") + 2);
						//checkVariable = checkLine.split("<")[0];
						checkLine=checkLine.split("<")[0];

						try{
							indexCheckLine=checkLine.indexOf('.');
							indexVersion=sVersion.indexOf('.');
							if(indexVersion>3){
								checkVariable=checkLatestVersion(checkLine, max);
							}else{
								if(indexCheckLine<4){
									checkVariable=checkLatestVersion(checkLine, max);
								}
							}

						}catch(Exception ex){
                            logger.error("Exception occurred : " + ex.getMessage());
						}
					}
				}
		}

				//Check whether the given version exists
				HttpURLConnection httpUrlConn;
				try{
					httpUrlConn = (HttpURLConnection) new URL(completeUrl+"/"+sVersion).openConnection();
					httpUrlConn.setRequestMethod("HEAD");
					httpUrlConn.setConnectTimeout(30000);
					httpUrlConn.setReadTimeout(30000);

					if(httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK==true){
						VersionManager versionManager = new VersionManager();
						versionManager.setValid();
					}
				}catch (Exception ex) {
                    logger.error("Exception occurred : " + ex.getMessage());
				}
			

		} catch (IOException ex) {
            logger.error("Exception occurred : " + ex.getMessage());
		}
		return checkVariable;
	}


	public String checkLatestVersion(String version, String max){

		String checkPatchVersion=null;
		String checkMinorVersion=null;
		String checkMajorVersion=null;
		String maxMajorVersion=null;
		String maxMinorVersion=null;
		String maxPatchVersion=null;
		String checkMinorLetters=null;
		String maxMinorLetters=null;
		String checkPatchLetters=null;
		String maxPatchLetters=null;

		if(version.contains(".wso2")){
			version=version.replace(".wso2", "-wso2");
		}else if(version.contains(".alpha")){
			version=version.replace(".alpha", "-alpha");
		}else if(version.contains(".beta")){
			version=version.replace(".beta", "-beta");
		}else if(version.contains("_POST")){
			version=version.replace("_POST", ".POST");
		}else if(version.contains(".v")){
			version=version.replace(".v", "-v");
		}

		String checkVersion=version;
		if(checkVersion.contains(".")){
			checkMajorVersion=checkVersion.substring(0, checkVersion.indexOf("."));
			String checkRest1=checkVersion.substring(checkVersion.indexOf(".")+1, checkVersion.length());;
			if(checkRest1.contains(".")){
				checkMinorVersion=checkRest1.substring(0, checkRest1.indexOf("."));
				String checkRest2=checkRest1.substring(checkRest1.indexOf(".")+1, checkRest1.length());
				if(checkRest2.contains(".")){
					checkPatchVersion=checkRest2.substring(0, checkRest2.indexOf("."));
				}else{
					checkPatchVersion=checkRest2;
				}
			}else{
				checkMinorVersion=checkRest1;
			}
		}else{
			checkMajorVersion=checkVersion;
		}

		if(max.contains(".")){
			maxMajorVersion=max.substring(0, max.indexOf("."));
			String maxrest1=max.substring(max.indexOf(".")+1, max.length());
			if(maxrest1.contains(".")){
				maxMinorVersion=maxrest1.substring(0, maxrest1.indexOf("."));
				String maxrest2=maxrest1.substring(maxrest1.indexOf(".")+1, maxrest1.length());
				if(maxrest2.contains(".")){
					maxPatchVersion=maxrest2.substring(0, maxrest2.indexOf("."));
				}else{
					maxPatchVersion=maxrest2;
				}
			}else{
				maxMinorVersion=maxrest1;
			}
		}else{
			maxMajorVersion=max;
		}

		if(Long.parseLong(checkMajorVersion)>Long.parseLong(maxMajorVersion)){ //1
			this.max=version;
			return this.max;
		}else if(Long.parseLong(checkMajorVersion)==Long.parseLong(maxMajorVersion)){ //2
			if(checkMinorVersion!=null){
				if (checkMinorVersion.contains("-")){
					checkMinorLetters=checkMinorVersion.substring(checkMinorVersion.indexOf('-')+1,
                            checkMinorVersion.length());
					checkMinorVersion=checkMinorVersion.substring(0, checkMinorVersion.indexOf('-'));	
				}
			}else{
				checkMinorLetters="0";
				checkMinorVersion="0";
			}

			if(maxMinorVersion!=null){
				if (maxMinorVersion.contains("-")){
					maxMinorLetters=maxMinorVersion.substring(maxMinorVersion.indexOf('-')+1, maxMinorVersion.length());
					maxMinorVersion=maxMinorVersion.substring(0, maxMinorVersion.indexOf('-'));	
				}
			}else{
				maxMinorLetters="0";
				maxMinorVersion="0";
			}

			if(Long.parseLong(checkMinorVersion)>Long.parseLong(maxMinorVersion)){ //4
				this.max=version;
				return this.max;
			}else if(Long.parseLong(checkMinorVersion)<Long.parseLong(maxMinorVersion)){ //6
				return this.max;
			}else if(Long.parseLong(checkMinorVersion)==Long.parseLong(maxMinorVersion)){ //5
				if(checkMinorLetters!=null && maxMinorLetters==null){ //7
					this.max=version;
					return this.max;
				}else if(checkMinorLetters==null && maxMinorLetters!=null){ //8
					return this.max;
				}else if(checkMinorLetters!=null && maxMinorLetters!=null
                        && checkMinorLetters.compareTo(maxMinorLetters)>0){ //9,11
					this.max=version;
					return this.max;
				}else if(checkMinorLetters!=null && maxMinorLetters!=null
                        && checkMinorLetters.compareTo(maxMinorLetters)<0){ //9,13
					return this.max;
				}else if(checkMinorLetters!=null && maxMinorLetters!=null
                        && checkMinorLetters.compareTo(maxMinorLetters)==0
                        || checkMinorLetters==null && maxMinorLetters==null){ //10,12
					if(checkPatchVersion!=null){
						if (checkPatchVersion.contains("-")){
							checkPatchLetters=checkPatchVersion.substring(checkPatchVersion.indexOf('-')+1,
                                    checkPatchVersion.length());
							checkPatchVersion=checkPatchVersion.substring(0, checkPatchVersion.indexOf('-'));	
						}
					}else{
						checkPatchLetters="0";
						checkPatchVersion="0";
					}

					if(maxPatchVersion!=null){
						if (maxPatchVersion.contains("-")){
							maxPatchLetters=maxPatchVersion.substring(maxPatchVersion.indexOf('-')+1,
                                    maxPatchVersion.length());
							maxPatchVersion=maxPatchVersion.substring(0, maxPatchVersion.indexOf('-'));	
						}
					}else{
						maxPatchLetters="0";
						maxPatchVersion="0";
					}

					if(Long.parseLong(checkPatchVersion)>Long.parseLong(maxPatchVersion)){ //14
						this.max=version;
						return this.max;
					}else if(Long.parseLong(checkPatchVersion)==Long.parseLong(maxPatchVersion)){ //15
						if(checkPatchLetters!=null && maxPatchLetters==null){ //17
							this.max=version;
							return this.max;
						}else if(checkPatchLetters==null && maxPatchLetters!=null){ //18
							return this.max;
						}else if(checkPatchLetters!=null && maxPatchLetters!=null
                                && checkPatchLetters.compareTo(maxPatchLetters)>0){ //19,23
							this.max=version;
							return this.max;
						}else if(checkPatchLetters!=null && maxPatchLetters!=null
                                && checkPatchLetters.compareTo(maxPatchLetters)<0){ //19,22
							return this.max;
						}else if(checkPatchLetters!=null && maxPatchLetters!=null
                                && checkPatchLetters.compareTo(maxPatchLetters)==0
                                || checkPatchLetters==null && maxPatchLetters==null){ //20,21
							return this.max;
						}
					}
				}
			}
		}
		return this.max;
	}

}
