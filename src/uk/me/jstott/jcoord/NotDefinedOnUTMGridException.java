package uk.me.jstott.jcoord;

/**
 * <p>
 * This exception is thrown when
 * </p> 
 * 
 * <p>
 * (c) 2006 Jonathan Stott
 * </p>
 *
 * <p>
 * Created on 12-Mar-2006
 * </p>
 *
 * @author Jonathan Stott
 * @version 1.1
 * @since 1.1
 */
public class NotDefinedOnUTMGridException extends RuntimeException {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 5699420767622348737L;

  
  /**
   * NotDefinedOnUTMGridException constructor.
   */
  public NotDefinedOnUTMGridException() {
    super();
  }

  
  /**
   * NotDefinedOnUTMGridException constructor with a message.
   * 
   * @param message details of the exception.
   */
  public NotDefinedOnUTMGridException(String message) {
    super(message);
  }

}
