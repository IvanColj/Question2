import java.io.IOException;

public class SecondUser {
    public static void main(String[] args) {
        SimpleUser user = new SimpleUser(50000,50001);
        try {
            user.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
