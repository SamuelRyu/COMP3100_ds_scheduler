import java.io.*;
import java.net.*;

// Singleton Design pattern that allows all classes to access the same Protocol
//  Protocol a single socket, din, and dout
public class Protocol{
    Socket s;
    DataInputStream din;
    DataOutputStream dout;

    private static Protocol instance = new Protocol();

    // private constructor ensures only one Protocol is created
    private Protocol() {
        try{
            s = new Socket("localhost", 50000);
            din = new DataInputStream(s.getInputStream());
            dout = new DataOutputStream(s.getOutputStream());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // Protocol object can be accessed by getInstance()
    public static Protocol getInstance(){
        return instance;
    }

    // Returns Protocol's DataInputStream
    public DataInputStream getDin(){
        return din;
    }

    // Returns Protocol's DataInputStream
    public DataOutputStream getDout(){
        return dout;
    }

    // Close socket, din, dout
    public void close() throws IOException{
        s.close();
        din.close();
        dout.close();
    }
}
