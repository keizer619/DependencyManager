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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.HttpURLConnection;
import java.net.URL;



public class UrlValidate {

    private static final Log logger = LogFactory.getLog(PatternMatch.class);

	String staticUrl=null;
	String groupIdUrl=null;
	String groupIdPlusUrl=null;
	String completeUrl=null;
	HttpURLConnection httpUrlConn;
	boolean status=false;

	public String checkUrl(String sArtifactId,String sGroupId,String sVersion){
		if(sVersion.contains("SNAPSHOT")){
			staticUrl="http://maven.wso2.org/nexus/content/repositories/snapshots/";
			sGroupId=sGroupId.replace('.', '/');
			groupIdUrl=staticUrl.concat(sGroupId);
			groupIdPlusUrl=groupIdUrl.concat("/");
			completeUrl=groupIdPlusUrl.concat(sArtifactId);
			if(checkUrlStatus(completeUrl)==true){
				status=false;
				return completeUrl;
			}else{
				staticUrl="http://maven.wso2.org/nexus/content/repositories/wso2.maven2.snapshot/";
				sGroupId=sGroupId.replace('.', '/');
				groupIdUrl=staticUrl.concat(sGroupId);
				groupIdPlusUrl=groupIdUrl.concat("/");
				completeUrl=groupIdPlusUrl.concat(sArtifactId);
				if(checkUrlStatus(completeUrl)==true){
					status=false;

					return completeUrl;
				}else{
					staticUrl="http://maven.wso2.org/nexus/content/groups/public/";
					sGroupId=sGroupId.replace('.', '/');
					groupIdUrl=staticUrl.concat(sGroupId);
					groupIdPlusUrl=groupIdUrl.concat("/");
					completeUrl=groupIdPlusUrl.concat(sArtifactId);
					if(checkUrlStatus(completeUrl)==true){
						status=false;
						return completeUrl;
					}
				}
			}

		}else{
			staticUrl = "http://mvnrepository.com/artifact/";
			groupIdUrl=staticUrl.concat(sGroupId);
			groupIdPlusUrl=groupIdUrl.concat("/");
			completeUrl=groupIdPlusUrl.concat(sArtifactId);		
			if(checkUrlStatus(completeUrl)==true){
				status=false;
				return completeUrl;
			}else{
				staticUrl="http://maven.wso2.org/nexus/content/groups/wso2-public/";
				sGroupId=sGroupId.replace('.', '/');
				groupIdUrl=staticUrl.concat(sGroupId);
				groupIdPlusUrl=groupIdUrl.concat("/");
				completeUrl=groupIdPlusUrl.concat(sArtifactId);	
				if(checkUrlStatus(completeUrl)==true){
					status=false;
					return completeUrl;
				}else{
					staticUrl="http://maven.wso2.org/nexus/content/repositories/wso2maven2/";
					sGroupId=sGroupId.replace('.', '/');
					groupIdUrl=staticUrl.concat(sGroupId);
					groupIdPlusUrl=groupIdUrl.concat("/");
					completeUrl=groupIdPlusUrl.concat(sArtifactId);
					if(checkUrlStatus(completeUrl)==true){
						status=false;
						return completeUrl;
					}else{
						staticUrl = "http://svn.wso2.org/repos/wso2/trunk/carbon/core/";
						groupIdUrl=staticUrl.concat(sArtifactId);
						completeUrl=groupIdUrl.concat("/pom.xml");	
						if(checkUrlStatus(completeUrl)==true){
							status=false;
							return completeUrl;
						}
					}
				}
			}
		}
		return null;
		}

	public boolean checkUrlStatus(String completeUrl){
		try{
			httpUrlConn = (HttpURLConnection) new URL(completeUrl+"/").openConnection();
			httpUrlConn.setRequestMethod("HEAD");
			httpUrlConn.setConnectTimeout(30000);
			httpUrlConn.setReadTimeout(30000);

			status=httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK;

		}catch (Exception ex) {
            logger.error("Exception occurred : " + ex.getMessage());
		}
		return status;
	}

}
