/** */
package com.strandls.utility.pojo;

import com.strandls.user.pojo.UserIbp;

/**
 * @author Abhishek Rudra
 */
public class FlagShow {

	private Flag flag;
	private UserIbp user;

	/** */
	public FlagShow() {
		super();
	}

	/**
	 * @param flag
	 * @param user
	 */
	public FlagShow(Flag flag, UserIbp user) {
		super();
		this.flag = flag;
		this.user = user;
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}

	public UserIbp getUser() {
		return user;
	}

	public void setUser(UserIbp user) {
		this.user = user;
	}
}
