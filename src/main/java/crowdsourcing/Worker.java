package crowdsourcing;

import java.util.ArrayList;

public class Worker {
	private String name;
	private int id;
	private ArrayList<String> factor;
	private ArrayList<String> cause;
	
	
	public Worker(String name, int id, ArrayList<String> factor, ArrayList<String> cause) {
		super();
		this.name = name;
		this.id = id;
		this.factor = factor;
		this.cause = cause;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public ArrayList<String> getFactor() {
		return factor;
	}


	public void setFactor(ArrayList<String> factor) {
		this.factor = factor;
	}


	public ArrayList<String> getCause() {
		return cause;
	}


	public void setCause(ArrayList<String> cause) {
		this.cause = cause;
	}
	
	
}
