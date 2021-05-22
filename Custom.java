import java.io.*;
import java.net.*;

public class Custom {
    Communication c;

    public Custom(Communication c){
        this.c = c;
    }

    public void runCustom() throws IOException{
        c.sendMsg("REDY");
        String job = c.readMsg();
        String[] jobSplit = job.split("\\s");
        c.sendMsg("GETS Capable " + jobSplit[4] + " " + jobSplit[5] + " " + jobSplit[6]);
        c.readMsg();
        c.sendMsg("OK");
        c.readMsg();
        c.sendMsg("OK");
        c.readMsg();
    }
}
