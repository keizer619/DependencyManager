package org.wso2.sample.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class FileSearch {

	static List<String> list = new ArrayList<String>();
	private String fileNameToSearch;
	private List<String> result = new ArrayList<String>();

	public String getFileNameToSearch() {
		return fileNameToSearch;
	}

	public void setFileNameToSearch(String fileNameToSearch) {
		this.fileNameToSearch = fileNameToSearch;
	}

	public List<String> getResult() {
		return result;
	}


	public static String[] getPath(String path){
		
		//DependencyVersion depV = new DependencyVersion();
		//depV.setPath("/home/nishali/Documents/snap/git");

		FileSearch fileSearch = new FileSearch();

		//try different directory and filename :)
		fileSearch.searchDirectory(new File(path), "pom.xml");

		int count = fileSearch.getResult().size();
		if(count ==0){
			System.out.println("\nNo result found!");
		}else{
			System.out.println("\nFound " + count + " result!\n");
			for (String matched : fileSearch.getResult()){
				list.add(matched);
			}
		}

		String[] d = new String[list.size()];

		for (int i = 0; i < list.size(); i++) {
			d[i] = list.get(i);
		}
		return d;
	}

	public void searchDirectory(File directory, String fileNameToSearch) {

		setFileNameToSearch(fileNameToSearch);
		if (directory.isDirectory()) {
			search(directory);
		} else {
			System.out.println(directory.getAbsoluteFile() + " is not a directory!");
		}
	}

	private void search(File file) {

		if (file.isDirectory()) {

			//do you have permission to read this directory?	
			if (file.canRead()) {
				for (File temp : file.listFiles()) {
					if (temp.isDirectory()) {
						search(temp);
					} else {
						if (getFileNameToSearch().equals(temp.getName().toLowerCase())) {			
							result.add(temp.getAbsoluteFile().toString());
						}
					}
				}

			} else {
				System.out.println(file.getAbsoluteFile() + "Permission Denied");
			}
		}
	}

	
	public String getParent(String path,String parentPath){
		int parentPathCount = parentPath.length();
		int pathCount=path.length();

		String parent = path.substring(parentPathCount+1,pathCount);
		int x = parent.indexOf('/');
		parent=parent.substring(0,x);
		return parent;
	}
	/*
  public static void main(String args[]){
	  
		DependencyVersion depV = new DependencyVersion();
		
	  int count = depV.getPath().length();
	  String[] abc = getPath();

	  for (int i =0;i<abc.length;i++){
		  System.out.println(abc[i]);
		  String sPath=abc[i];

		  int pathCount=sPath.length();
				  
		  String parent=sPath.substring(count+1,pathCount);
		  int x = parent.indexOf('/');
		  parent=parent.substring(0,x);		  
		  
	  }

  }
  */
	 

}
