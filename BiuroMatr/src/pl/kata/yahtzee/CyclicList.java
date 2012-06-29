package pl.kata.yahtzee;

import java.util.Iterator;

public class CyclicList<T> implements Iterable<T> {
	private class ListNode<S> {
		public S data;
		public ListNode<S> next;
		public ListNode(S data2) {
			data = data2;
		}
	}
	
	private class FiniteIterator<S> implements Iterator<S> {
		private ListNode<S> start;
		private ListNode<S> current;

		public FiniteIterator(ListNode<S> start1) {
			this.start = start1;
			this.current = null;
		}
		
		@Override
		public boolean hasNext() {
			return current == null || current.next != start;
		}

		@Override
		public S next() {
			if(current == null) {
				current = start;
			} else {
				current = current.next;
			}
			return current.data;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
		}
	}
	
	private Integer size;
	private ListNode<T> current;
	
	public CyclicList() {
		this.size = 0;
		this.current = null;
	}
	
	public void add(T data) {
		ListNode<T> node = new ListNode<T>(data);
		if(this.current == null) {
			this.current = node;
			node.next = node;
		} else {
			node.next = this.current.next;
			this.current.next = node;
		}
		this.size++;
	}
	
	public T getCurrent() {
		return this.current.data;
	}
	
	public void next() {
		this.current = this.current.next;
	}
	
	public Integer getSize() {
		return size;
	}

	@Override
	public Iterator<T> iterator() {
		return new FiniteIterator<T>(this.current);
	}
	
}
