package mvc;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException(String msg) {
        super(msg);
    }

    public PathNotFoundException() {

    }

}
