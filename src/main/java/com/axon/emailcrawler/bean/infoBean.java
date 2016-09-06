package com.axon.emailcrawler.bean;

import org.jsoup.nodes.Document;


public class infoBean {

    private String email;
    private String name;
    private Document doc;
	public Document getDoc() {
		return doc;
	}
	public void setDoc(Document doc) {
		this.doc = doc;
	}
	@Override
	public String toString() {
		return "infoBean [email=" + email + ", name=" + name + ", doc=" + doc
				+ "]";
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

   
}
