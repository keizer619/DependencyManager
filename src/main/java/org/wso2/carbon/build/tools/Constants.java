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

package org.wso2.carbon.build.tools;

public class Constants {

    public static final String XPATH_ARTIFACT_SOURCE = "/project/artifactId";
    public static final String XPATH_GROUP_ID = "/project/groupId";
    public static final String XPATH_VERSION = "/project/version";
    public static final String XPATH_PARENT_GROUP_ID = "/project/parent/groupId";
    public static final String XPATH_PARENT_VERSION = "/project/parent/version";

    public static final String ROOT_PATH = "/Users/tharik/Desktop/git/rep";
    public static boolean IS_ALL_POMS = false;
    public static String POM_FILE_NAME = "pom.xml";
    
    public static final String M2_PATH = "/Users/tharik/.m2/repository";
    public static final String DEFAULT_SOURCE_NAME = "Other";
    public static final String DEPENDENCY_SEPERATOR = ":";
    
    public static final String READ_FILE_PATH="/home/nishali/Documents/Git/DependencyManager/out.txt";
}
