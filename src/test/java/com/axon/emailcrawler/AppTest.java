package com.axon.emailcrawler;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest {
	public static void main(String[] args) {
		String s = "www.lagou.com/zhaopin/Java/2/";
		String[] ssStrings = s.split("/");
		//System.out.println(ssStrings.length);
		StringBuilder sBuilder = new StringBuilder();
		for(int i=0;i<ssStrings.length-1;i++){
		sBuilder.append(ssStrings[i]).append("/");
		}
		String prifx = sBuilder.toString();
		System.out.println(prifx);
		for (int i = 2; i <= Integer.parseInt("30"); i++) {
			System.out.println(prifx+i+"/");
			
			
		}
	}
}
