package org.my.facebookalgorithm;

public class FriendsPair {
	
	private String name1;
	
	//private String id1;
	private String name2;
	//private String id2;
	private String commonEvent;
	
	private Long numOfMutualFriends;

	public FriendsPair(String name1, String name2, String commonEvent)
	{
	    this.name1 = name1;
	    this.name2 = name2;
	    this.commonEvent=commonEvent;
	    //this.id1 = id1;
	    //this.id2 = id2;	  
	    this.numOfMutualFriends =(long)0;
	}	
	public String getName1() {
		return name1;
	}

	public void setName1(String name1) {
		this.name1 = name1;
	}

	/*public String getId1() {
		return id1;
	}

	public void setId1(String id1) {
		this.id1 = id1;
	}*/

	public String getName2() {
		return name2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}
	public String getCommonEvent() {
		return commonEvent;
	}
	public void setCommonEvent(String commonEvent) {
		this.commonEvent = commonEvent;
	}

	public Long getNumOfMutualFriends() {
		return numOfMutualFriends;
	}
	public void setNumOfMutualFriends(Long numOfMutualFriends) {
		this.numOfMutualFriends = numOfMutualFriends;
	}
	/*public String getId2() {
		return id2;
	}

	public void setId2(String id2) {
		this.id2 = id2;
	}*/	

}
