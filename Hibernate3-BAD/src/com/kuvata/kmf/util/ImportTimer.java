package com.kuvata.kmf.util;

import java.util.Date;
import java.util.Timer;

public class ImportTimer extends Timer
{
	public Date importDate;

	/**
	 * @return Returns the importDate.
	 */
	public Date getImportDate() {
		return importDate;
	}
	

	/**
	 * @param importDate The importDate to set.
	 */
	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}
	
	
}