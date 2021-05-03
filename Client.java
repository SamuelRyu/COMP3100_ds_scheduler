import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;


class Client {

    public static void sendMsg(DataOutputStream dout, String msg) {
        try {
            byte[] message = msg.getBytes();
            message = Arrays.copyOf(message, message.length + 1);
            message[message.length - 1] = 10;
            dout.write(message);
            System.out.println("SENT " + msg);
            dout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readMsg(DataInputStream din) {
        String message = "";
        try {
            byte inBytes[] = new byte[din.available() + 64];
            din.read(inBytes);
            for (int i = 0; i < inBytes.length; i++) {
                message += (char) inBytes[i];
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
        System.out.println("RCVD " + message);
        return message;
    }


    public static void performHandshake(DataInputStream din, DataOutputStream dout) {
        sendMsg(dout, "HELO");
        readMsg(din);
        sendMsg(dout, "AUTH " + System.getProperty("user.name"));
        readMsg(din);
    }

    public static void main(String args[]) throws Exception {
        Socket s = new Socket("localhost", 50000);
        DataInputStream din = new DataInputStream(s.getInputStream());
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());

        // Start handshake
        performHandshake(din, dout);

        // Ready to start receiving jobs
        sendMsg(dout, "REDY");
        String response = readMsg(din);
        String[] job = response.split("\\s+");


        sendMsg(dout, "GETS Capable " + job[4] + " " + job[5] + " "+ job[6]);
        readMsg(din);
        
        sendMsg(dout, "OK");
        response = readMsg(din);
        String[] server = response.split("\\s+");
        ArrayList<Server> serverList = new ArrayList<Server>();

        for (int i = 0; i < server.length - 1; i += 9){
            Server newServer = new Server();
            newServer.setServerType(server[i]);
            newServer.setServerID(Integer.parseInt(server[i+1]));
            newServer.setState(server[i+2]);
            newServer.setCurStartTime(Integer.parseInt(server[i+3]));
            newServer.setCore(Integer.parseInt(server[i+4]));
            newServer.setMem(Integer.parseInt(server[i+5]));
            newServer.setDisk(Integer.parseInt(server[i+6]));
            newServer.setWJobs(Integer.parseInt(server[i+7]));
            newServer.setRJobs(Integer.parseInt(server[i+8]));
            serverList.add(newServer);
        }

        for(int i = 0; i < serverList.size(); i ++ ){
            System.out.print(serverList.get(i).serverType + " ");
            System.out.print(serverList.get(i).serverID + " ");
            System.out.print(serverList.get(i).state + "\n\n");
        }

        sendMsg(dout, "OK");
        readMsg(din);

        sendMsg(dout, "SCHD " + job[2] + " " + server[0] + " " + server[1]);
        readMsg(din);

        sendMsg(dout, "REDY");
        readMsg(din);

        // Quit
        sendMsg(dout, "QUIT");
        readMsg(din);

        din.close();
        dout.close();
        s.close();
    }

}