/** */
package com.strandls.utility.pojo;

/**
 * @author Abhishek Rudra
 */
public class FlagIbp {

	private String flag;
	private String notes;

	/** */
	public FlagIbp() {
		super();
	}

	/**
	 * @param flag
	 * @param notes
	 */
	public FlagIbp(String flag, String notes) {
		super();
		this.flag = flag;
		this.notes = notes;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
