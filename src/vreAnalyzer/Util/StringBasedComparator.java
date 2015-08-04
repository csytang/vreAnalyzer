package vreAnalyzer.Util;

import java.util.Comparator;


/** Generic comparator for objects based on the result of toString. Null is the "minimum". */
public class StringBasedComparator<T> implements Comparator<T>{
	public int compare(T o1, T o2) {
		if (o1 == null)
			return (o2 == null)? 0 : -1; // o1 is equal to or less than o2
		if (o2 == null)
			return 1; // o1 is greater than o2
		return o1.toString().compareTo(o2.toString());
	}
}