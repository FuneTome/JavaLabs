import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    private static double computeIntegral(double a, double b, double step) {
        double res = 0.0;
        double current = a;
        double next;
        while (current + step < b) {
            next = current + step;
            res += ((f(current) + f(next)) / 2.0) * step;
            current = next;
        }
        double lastStep = b - current;
        if (lastStep > 1e-15) {
            res += ((f(current) + f(b)) / 2.0) * lastStep;
        }
        return res;
    }

    private static double f(double x) {
        return 1.0 / Math.log(x);
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8000;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        try (Socket socket = new Socket(host, port);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            double lower = dis.readDouble();
            double upper = dis.readDouble();
            double step = dis.readDouble();

            double partialResult = computeIntegral(lower, upper, step);

            dos.writeDouble(partialResult);
            dos.flush();

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}