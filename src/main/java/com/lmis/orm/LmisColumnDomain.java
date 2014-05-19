package com.lmis.orm;

public class LmisColumnDomain {
	String operator, value;

	public LmisColumnDomain(String operator, String value) {
		this.operator = operator;
		this.value = value;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
