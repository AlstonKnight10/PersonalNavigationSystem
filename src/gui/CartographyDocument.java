package gui;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class representing a cartography document, which contains a collection of elements (of type T)
 * and a bounding rectangle that defines the spatial extent of those elements. The document also
 * maintains a separate collection of highlighted elements, allowing for certain elements to be
 * visually distinguished from others when rendered by a Cartographer.
 * 
 * @param <T>
 *          The type of the elements contained in the document.
 */
public class CartographyDocument<T> implements Iterable<T>
{

  protected Map<String, T> highlighted;
  protected Map<String, T> elements;
  protected Rectangle2D.Double bounds;

  /**
   * Explicit Value Constructor.
   * 
   * @param elements
   *          A map of element IDs to elements. Must not be null.
   * @param bounds
   *          The bounding rectangle that defines the spatial extent of the elements. Must not be
   *          null.
   */
  public CartographyDocument(final Map<String, T> elements, final Rectangle2D.Double bounds)
  {
    if (elements == null || bounds == null)
    {
      throw new IllegalArgumentException("Elements and bounds cannot be null");
    }
    this.elements = elements;
    this.bounds = bounds;
    this.highlighted = new LinkedHashMap<>();
  }

  /**
   * Returns the bounding rectangle that defines the spatial extent of the elements in this
   * document.
   * 
   * @return The bounding rectangle of the document's elements.
   */
  public Rectangle2D.Double getBounds()
  {
    return this.bounds;
  }

  /**
   * Returns the element associated with the given ID, or null if no such element exists in the
   * document.
   * 
   * @param id
   *          The unique identifier of the element to retrieve. Must not be null.
   * @return The element associated with the given ID, or null if no such element exists in the
   *         document.
   */
  public T getElement(final String id)
  {
    if (id == null)
    {
      throw new IllegalArgumentException("ID cannot be null");
    }
    return this.elements.get(id);
  }

  /**
   * Returns an iterator over the elements in this document that are currently highlighted. The
   * order of iteration is determined by the order of insertion into the highlighted map.
   * 
   * @return An iterator over the highlighted elements in this document.
   */
  public Iterator<T> highlighted()
  {
    return this.highlighted.values().iterator();
  }

  @Override
  public Iterator<T> iterator()
  {
    return this.elements.values().iterator();
  }

  /**
   * Sets the highlighted elements in this document to the given map of element IDs to elements. The
   * provided map must not be null, and should contain only valid element IDs that exist in the
   * document's elements map. This method replaces any existing highlighted elements with the new
   * set of highlighted elements provided in the argument.
   * 
   * @param highlighted
   *          A map of element IDs to elements that should be highlighted. Must not be null, and
   *          should contain only valid element IDs that exist in the document's elements map.
   */
  public void setHighlighted(final Map<String, T> highlighted)
  {
    if (highlighted == null)
    {
      throw new IllegalArgumentException("Highlighted map cannot be null");
    }
    this.highlighted = highlighted;
  }
}
