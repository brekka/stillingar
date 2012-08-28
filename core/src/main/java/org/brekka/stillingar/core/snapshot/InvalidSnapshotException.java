/**
 * 
 */
package org.brekka.stillingar.core.snapshot;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class InvalidSnapshotException extends Exception {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3596870371764252161L;

    /**
     * @param message
     * @param cause
     */
    public InvalidSnapshotException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public InvalidSnapshotException(String message) {
        super(message);
    }

    
}
