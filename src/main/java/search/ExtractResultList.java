package search;

import java.util.ArrayList;

public class ExtractResultList {
	private ArrayList<ExtractResult> list;
	
	
	public ExtractResultList() {
		super();
		this.list = new ArrayList<ExtractResult>();
	}

	public ExtractResult getById(String id) {
		for(ExtractResult er: list) {
			if(er.getId().contains(id)) {
				return er;
			}
		}
		return null;
	}
	
	public ExtractResult get(int i) {
		return list.get(i);
	}
	
	public void add(ExtractResult er) {
		list.add(er);
	}
	
	public boolean contains(String id) {
		boolean b = false;
		if(list.size() == 0) {
			return false;
		}
		for(ExtractResult er: list) {
			if(er.getId().equals(id)) {
				b = true;
			}
		}
		return b;
	}
	
	public int size() {
		int i = 0;
		return list.size();
	}

	
}
