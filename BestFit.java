import java.io.*;
import java.net.*;
import java.util.ArrayList;

// Best Fit implementation
public class BestFit {
    Communication c;

    // Best fit requires singleton instance communication
    public BestFit(Communication c){
        this.c = c;
    }

    // throws IOException inorder to use socket, din, dout
    public void runBestFit() throws IOException{

        // Send first ready
        c.sendMsg("REDY");
        String job = c.readMsg();

        // If the response is not none
        while(!job.contains("NONE")){

            // If message indicates there is a job
            if (job.contains("JOBN")){
                

                // Split message to array of strings
                // jobn | sub_time | job_id | estimated_time | cores | mem | disk
                String[] jobSplit = job.split("\\s");

                // Request Capable servers
                c.sendMsg("GETS Capable " + jobSplit[4] + " " + jobSplit[5] + " " + jobSplit[6]);
                c.readMsg();
                c.sendMsg("OK");

                // Split server String
                String server = c.readMsg();
                String[] serverSplit = server.split("\\s");

                // Create list of capable servers 
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

                // Least fitness value
                //   initialising as first server fitness value
                int leastFit = serverList.get(0).getCore() - Integer.parseInt(jobSplit[4]);
                // Server with least fitness value
                Server bestServer = serverList.get(0);

                // For each server, find fitness (server core - job cores),
                //   eventually finds server with smallest fitness value
                //   tries to match servers with jobs that have similar/same cores
                for(Server i : serverList){
                    int fitnessValue = i.getCore() - Integer.parseInt(jobSplit[4]);
                    // If lowest fitness value is negative, grab next server until positive
                    if (leastFit < 0){ 
                        leastFit = fitnessValue;
                        bestServer = i;
                    } else if ((fitnessValue < leastFit) && i.getWJobs() < bestServer.getWJobs()){
                        leastFit = fitnessValue;
                        bestServer = i;
                    }
                    
                }            

                c.sendMsg("OK");
                c.readMsg();

                // Schedule job with best matching server
                c.sendMsg("SCHD " + jobSplit[2] + " " + bestServer.getServerType() + " " + bestServer.getServerID());
                c.readMsg();
            }

            // Send server the client is REDY for next job
            c.sendMsg("REDY");
            job = c.readMsg();
        }

    }
}