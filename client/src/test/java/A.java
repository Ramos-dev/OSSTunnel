import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class A {

    static int port = 1;

    public static void main(String[] args) {

        for (int i = 1; i < 65534; i++) {
            new A.AcceptThread().start();

            port++;
        }


    }

    static class AcceptThread extends Thread {

        ServerSocket serverSocket;

        {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            Socket socket = null;
            DataInputStream input = null;

            try {
                socket = serverSocket.accept();
                socket.setKeepAlive(true);

                while (true) {
                    input = new DataInputStream(socket.getInputStream());
                    byte[] buffer = {};
                    int bufflenth = input.available();
                    int size = 0;
                    while (bufflenth != 0) {
                        // 初始化byte数组为buffer中数据的长度
                        buffer = new byte[bufflenth];
                        size += input.read(buffer);
                        bufflenth = input.available();
                    }
                    if (buffer.length != 0) {

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                        input = null;
                    }
                    if (socket != null) {
                        socket.close();
                        socket = null;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
