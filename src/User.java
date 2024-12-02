import java.io.IOException;

public interface User extends AutoCloseable {
    void getM();
    void sendM(String sentence);
    void start() throws IOException;

}
