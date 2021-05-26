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

        // Read command line arguments
        String alg = new String();
        if (args.length > 0){
            alg = args[0];
        } else {
            System.out.println("Please select an algorithm");
            System.exit(1);
        }
        
        // Choose Algorithm
        if (alg.equals("-cf")){
            CustomFit f = new CustomFit(c);
            f.runCustomFit();
        } else {
            System.out.println("Algorithm doesn't exist.");
            System.exit(1);
        }
        
        
        p.close();
    }

}