import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;


// Uses the singleton instance and builds methods on top of it
public class Communication {
    DataInputStream din;
    DataOutputStream dout;

    public Communication(DataInputStream din,DataOutputStream dout){
        this.din = din;
        this.dout = dout;
    }

    // Send message method
    //  converts message to byte array, increases array size and appends value 10 to last index
    //  sends message with new line character
    public void sendMsg(String msg) throws IOException {

            byte[] message = msg.getBytes();
            message = Arrays.copyOf(message, message.length + 1);
            message[message.length - 1] = 10;
            dout.write(message);
            // System.out.println("SENT " + msg);
            dout.flush();

    }

    // Read message
    //  creates byte array with size of the estimate number of bytes that can be read, additional size for security
    //  adds each byte casted to String
    public String readMsg() throws IOException{
        String message = din.readLine();

        while(din.available() > 0){
            message += "\n" + din.readLine();
        }

        // System.out.println("RCVD " + message);

        return message;
    }
}
