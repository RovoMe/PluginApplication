package at.rovo.core.filemonitor;

/**
 * Possible actions that could happen to a file.
 */
public enum FileAction
{
    /** Indicates a file creation **/
    FILE_CREATED,

    /** Indicates an update of the file **/
    FILE_MODIFIED,

    /** Indicates that the file was removed **/
    FILE_DELETED
}
