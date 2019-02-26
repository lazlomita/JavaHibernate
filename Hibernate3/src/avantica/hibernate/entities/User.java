package avantica.hibernate.entities;

import java.io.Serializable;

public class User implements Serializable {
	
	private String username;
	private Address address;
	
	public User() {}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}
}