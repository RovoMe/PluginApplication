package at.rovo.core.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Simple mapper from {@link Iterator} to {@link Enumeration}.
 *
 * @param <E>
 *         The type of the Enumerator
 *
 * @author Roman Vottner
 */
public class IteratorEnumeration<E> implements Enumeration<E>
{
    /** The iterator to map **/
    private final Iterator<E> iterator;

    /**
     * Creates a new mapping from {@link Iterator} to {@link Enumeration} and initializes the iterator containing the
     * elements which should act as enumeration elements.
     *
     * @param iterator
     *         The iterator of the {@link List} or {@link Set} to map
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
