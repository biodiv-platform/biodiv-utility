package com.strandls.utility.pojo;

import com.strandls.activity.pojo.MailData;

public class FlagCreateData {

	private FlagIbp flagIbp;
	private MailData mailData;

	/** */
	public FlagCreateData() {
		super();
	}

	/**
	 * @param flagIbp
	 * @param mailData
	 */
	public FlagCreateData(FlagIbp flagIbp, MailData mailData) {
		super();
		this.flagIbp = flagIbp;
		this.mailData = mailData;
	}

	public FlagIbp getFlagIbp() {
		return flagIbp;
	}

	public void setFlagIbp(FlagIbp flagIbp) {
		this.flagIbp = flagIbp;
	}

	public MailData getMailData() {
		return mailData;
	}

	public void setMailData(MailData mailData) {
		this.mailData = mailData;
	}
}
