import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class CustomFit {
    Communication c;

    public CustomFit(Communication c){
        this.c = c;
    }

    public void runCustomFit() throws IOException{
        c.sendMsg("REDY");
        String job = c.readMsg();
        String[] jobSplit = job.split("\\s");
        c.sendMsg("GETS Capable " + jobSplit[4] + " " + jobSplit[5] + " " + jobSplit[6]);
        c.readMsg();
        c.sendMsg("OK");
        String server = c.readMsg();
        String[] serverSplit = server.split("\\s");
        ArrayList<Server> serverList = new ArrayList<Server>();
        for (int i = 0; i < serverSplit.length; i += 9){
            if ((serverSplit.length - i) >= 9){
                Server s = new Server();
                s.setServerType(serverSplit[i]);
                s.setServerID(Integer.parseInt(serverSplit[i+1]));
                s.setState(serverSplit[i+2]);
                s.setCurStartTime(Integer.parseInt(serverSplit[i+3]));
                s.setCore(Integer.parseInt(serverSplit[i+4]));
                s.setMem(Integer.parseInt(serverSplit[i+5]));
                s.setDisk(Integer.parseInt(serverSplit[i+6]));
                s.setWJobs(Integer.parseInt(serverSplit[i+7]));
                s.setRJobs(Integer.parseInt(serverSplit[i+8]));
                serverList.add(s);
            }
        }

        for(Server i : serverList){
            System.out.println(i.getServerType());
        }
        
        c.sendMsg("OK");
        c.readMsg();
    }
}
