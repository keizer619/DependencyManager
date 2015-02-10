package org.wso2.dependency;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author 		W.A.T. Nishali Wijesinghe	(nishaliw@wso2.com)				
 * @since 		2015-02-06 				
 */

public class UrlValidate {

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


		System.out.println("Can't find the dependency url");
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
			System.out.println("Exception");
		}
		return status;
	}

}
