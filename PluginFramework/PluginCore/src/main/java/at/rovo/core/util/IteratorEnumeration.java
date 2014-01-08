package at.rovo.core.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * <p>
 * Simple mapper from {@link Iterator} to {@link Enumeration}.
 * </p>
 * 
 * @author Roman Vottner
 *
 * @param <E> The type of the Enumerator
 */
public class IteratorEnumeration<E> implements Enumeration<E>
{
	/** The iterator to map **/
	private final Iterator<E> iterator;
	
	/**
	 * <p>
	 * Creates a new mapping from {@link Iterator} to {@link Enumeration} and
	 * initializes the iterator containing the elements which should act as
	 * enumeration elements.
	 * </p>
	 * 
	 * @param iterator The iterator of the {@link List} or {@link Set} to map
	 */
	public IteratorEnumeration(Iterator<E> iterator)
	{
		this.iterator = iterator;
	}
	
	@Override
	public boolean hasMoreElements()
	{
		return iterator.hasNext();
	}

	@Override
	public E nextElement()
	{
		return iterator.next();
	}

}
