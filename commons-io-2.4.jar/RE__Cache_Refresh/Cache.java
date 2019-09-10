package com.cache;

public class Cache {
	
	private String process = null;
	private String resultCode = null;
	private String resultDesc = null;
	private String remarks = null;
	
	
	public Cache (String p_process, String p_resultCode, String p_resultDesc, String p_remarks){
		this.process = p_process;
		this.resultCode = p_resultCode;
		this.resultDesc = p_resultDesc;
		this.remarks = p_remarks;
	}
	
	
	/**
	 * @return the process
	 */
	public String getProcess() {
		return process;
	}
	/**
	 * @param process the process to set
	 */
	public void setProcess(String process) {
		this.process = process;
	}
	/**
	 * @return the resultCode
	 */
	public String getResultCode() {
		return resultCode;
	}
	/**
	 * @param resultCode the resultCode to set
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	/**
	 * @return the resultDesc
	 */
	public String getResultDesc() {
		return resultDesc;
	}
	/**
	 * @param resultDesc the resultDesc to set
	 */
	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}


	/**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}
	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
