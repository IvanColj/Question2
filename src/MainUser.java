import java.io.IOException;

public class MainUser {
    public static void main(String[] args) {
        SimpleUser user = new SimpleUser(50001,50000);
        try {
            user.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
