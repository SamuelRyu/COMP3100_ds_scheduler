import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;


class Client {
    public static void main(String args[]) throws Exception {
        Protocol p = Protocol.getInstance();
        Communication c = new Communication(p.din, p.dout);

        // Start handshake
        c.sendMsg("HELO");
        c.readMsg();
        c.sendMsg("AUTH " + System.getProperty("user.name"));
        c.readMsg();


        p.close();
    }

}