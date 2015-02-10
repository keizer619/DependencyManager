package org.wso2.dependency;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.HttpURLConnection;

import org.wso2.dependency.VersionManager;
import org.wso2.sample.Constants;

/**
 * @author 		W.A.T. Nishali Wijesinghe	(nishaliw@wso2.com)				
 * @since 		2015-02-06 				
 */

public class PatternMatch {

	String patternStringLatest=null;
	Pattern patternLatest=null;
	Matcher matcherLatest=null;
	boolean matchesLatest=false;

	boolean valid = false;
	BufferedReader buffReader = null;

	String checkVariable=null;
	String sCurrentLine=null;
	String checkLine=null;
	InputStream inStream = null;
	String digits=null;
	long digitInt=0;
	String max="0.0";

	public String getMAtchDetails(String sGroupId,String sVersion,String completeUrl,String repository){ 
		try {
			buffReader = new BufferedReader(new FileReader(Constants.READ_FILE_PATH));

			if(repository.equalsIgnoreCase("wso2")){
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
					System.out.println("Exception");
				}

			}else{
				while ((sCurrentLine = buffReader.readLine()) != null) {
					String text = sCurrentLine;
					if (sCurrentLine.contains("class=\"vbtn")){
						checkLine = text.substring(text.lastIndexOf("\"") + 2);
						checkVariable = checkLine.split("<")[0];
						break;
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
					System.out.println("Exception");
				}
			}

		} catch (IOException e) {
			System.out.println("Error in Pattern Match");
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

		//=============================================================================================


		if(Long.parseLong(checkMajorVersion)>Long.parseLong(maxMajorVersion)){ //1
			this.max=version;
			return this.max;
		}else if(Long.parseLong(checkMajorVersion)==Long.parseLong(maxMajorVersion)){ //2
			if(checkMinorVersion!=null){
				if (checkMinorVersion.contains("-")){
					checkMinorLetters=checkMinorVersion.substring(checkMinorVersion.indexOf('-')+1, checkMinorVersion.length());
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
				checkMinorLetters=null;
				maxMinorLetters=null;
				return this.max;
			}else if(Long.parseLong(checkMinorVersion)<Long.parseLong(maxMinorVersion)){ //6
				checkMinorLetters=null;
				return this.max;
			}else if(Long.parseLong(checkMinorVersion)==Long.parseLong(maxMinorVersion)){ //5
				if(checkMinorLetters!=null && maxMinorLetters==null){ //7
					checkMinorLetters=null;
					this.max=version;
					return this.max;
				}else if(checkMinorLetters==null && maxMinorLetters!=null){ //8
					maxMinorLetters=null;
					return this.max;
				}else if(checkMinorLetters!=null && maxMinorLetters!=null && checkMinorLetters.compareTo(maxMinorLetters)>0){ //9,11
					this.max=version;
					checkMinorLetters=null;
					maxMinorLetters=null;
					return this.max;
				}else if(checkMinorLetters!=null && maxMinorLetters!=null && checkMinorLetters.compareTo(maxMinorLetters)<0){ //9,13
					checkMinorLetters=null;
					maxMinorLetters=null;
					return this.max;
				}else if(checkMinorLetters!=null && maxMinorLetters!=null && checkMinorLetters.compareTo(maxMinorLetters)==0 || checkMinorLetters==null && maxMinorLetters==null){ //10,12
					if(checkPatchVersion!=null){
						if (checkPatchVersion.contains("-")){
							checkPatchLetters=checkPatchVersion.substring(checkPatchVersion.indexOf('-')+1, checkPatchVersion.length());
							checkPatchVersion=checkPatchVersion.substring(0, checkPatchVersion.indexOf('-'));	
						}
					}else{
						checkPatchLetters="0";
						checkPatchVersion="0";
					}

					if(maxPatchVersion!=null){
						if (maxPatchVersion.contains("-")){
							maxPatchLetters=maxPatchVersion.substring(maxPatchVersion.indexOf('-')+1, maxPatchVersion.length());
							maxPatchVersion=maxPatchVersion.substring(0, maxPatchVersion.indexOf('-'));	
						}
					}else{
						maxPatchLetters="0";
						maxPatchVersion="0";
					}

					if(Long.parseLong(checkPatchVersion)>Long.parseLong(maxPatchVersion)){ //14
						maxMinorLetters=null;
						this.max=version;
						return this.max;
					}else if(Long.parseLong(checkPatchVersion)==Long.parseLong(maxPatchVersion)){ //15
						if(checkPatchLetters!=null && maxPatchLetters==null){ //17
							checkPatchLetters=null;
							this.max=version;
							return this.max;
						}else if(checkPatchLetters==null && maxPatchLetters!=null){ //18
							maxPatchLetters=null;
							return this.max;
						}else if(checkPatchLetters!=null && maxPatchLetters!=null && checkPatchLetters.compareTo(maxPatchLetters)>0){ //19,23
							this.max=version;
							checkPatchLetters=null;
							maxPatchLetters=null;
							return this.max;
						}else if(checkPatchLetters!=null && maxPatchLetters!=null && checkPatchLetters.compareTo(maxPatchLetters)<0){ //19,22
							checkPatchLetters=null;
							maxPatchLetters=null;
							return this.max;
						}else if(checkPatchLetters!=null && maxPatchLetters!=null && checkPatchLetters.compareTo(maxPatchLetters)==0 || checkPatchLetters==null && maxPatchLetters==null){ //20,21
							checkPatchLetters=null;
							maxPatchLetters=null;
							return this.max;
						}
					}
				}
			}
		}
		return this.max;
	}

}
