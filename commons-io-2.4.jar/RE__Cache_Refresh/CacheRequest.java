package com.cache;

import java.util.List;

public class CacheRequest {
	
	private String requestNo = null;
	private String resultCode = null;
	private String resultDesc = null;
	private String appInstance = null;
	private String response = null;
	
	private List<Cache> caches = null;
	
	
	
	/**
	 * @return the requestNo
	 */
	public String getRequestNo() {
		return requestNo;
	}
	/**
	 * @param requestNo the requestNo to set
	 */
	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
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
	 * @return the appInstance
	 */
	public String getAppInstance() {
		return appInstance;
	}
	/**
	 * @param appInstance the appInstance to set
	 */
	public void setAppInstance(String appInstance) {
		this.appInstance = appInstance;
	}
	
	
	
	/**
	 * @return the result
	 */
	public String getResponse() {
		return response;
	}
	/**
	 * @param result the result to set
	 */
	public void setResponse (String response) {
		this.response = response;
	}
	
	
	
	/**
	 * @return the caches
	 */
	public List<Cache> getCaches() {
		return caches;
	}
	/**
	 * @param caches the caches to set
	 */
	public void setCaches(List<Cache> caches) {
		this.caches = caches;
	}

}
