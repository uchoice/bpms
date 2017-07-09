package net.uchoice.common.entity;

public class JsonResult {

	private String status;
	
	private Object result;
	
	

	public JsonResult() {
	}
	
	public JsonResult(String status, Object result) {
		super();
		this.status = status;
		this.result = result;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
	
	public static JsonResult success(String message){
		return new JsonResult("1", message);
	}
	
	public static JsonResult fail(String message){
		return new JsonResult("0", message);
	}
	
}
