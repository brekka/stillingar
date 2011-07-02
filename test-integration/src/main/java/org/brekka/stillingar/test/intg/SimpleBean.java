package org.brekka.stillingar.test.intg;

public class SimpleBean {

	private Float value;

	public void setValue(Float value) {
		this.value = value;
		
		System.out.println("SimpleBean value changed: " + value);
	}
	
	@Override
	public String toString() {
		return "The value: " + value;
	}
}
