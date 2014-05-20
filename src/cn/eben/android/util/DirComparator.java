package cn.eben.android.util;

import java.util.Comparator;

public class DirComparator implements Comparator<String> {

	public int compare(String name1, String name2) {
	    // TODO Auto-generated method stub
	    String[] s1 = name1.split("/");
	    String[] s2 = name2.split("/");

	    return (s2.length - s1.length);
	}

}
