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
        if (alg.equals("-ff")){
            FirstFit f = new FirstFit(c);
            f.runFirstFit();
        } else if(alg.equals("-bf")){
            BestFit f = new BestFit(c);
            f.runBestFit();
        } else if(alg.equals("-wf")){
            WorstFit f = new WorstFit(c);
            f.runWorstFit();
        }
        
        
        p.close();
    }

}