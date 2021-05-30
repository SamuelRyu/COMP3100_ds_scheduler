import java.io.*;
import java.net.*;
import java.util.ArrayList;

// Prioritise Idle implementation
public class PrioritiseIdle {
    Communication c;

    // Prioritise idle requires singleton instance communication
    public PrioritiseIdle(Communication c){
        this.c = c;
    }

    // throws IOException inorder to use socket, din, dout
    public void runPrioritiseIdle() throws IOException{

        // Send first ready
        c.sendMsg("REDY");
        String job = c.readMsg();

        // If response is not NONE
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

                
                String server = c.readMsg();
                
                c.sendMsg("OK");
                c.readMsg();

                // Split server String
                String[] serverSplit = server.split("\\s");

                // Create list of servers
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

                // Create deepcoppy array of server list
                ArrayList<Server> deepCopy = new ArrayList<Server>();

                for (Server s: serverList){
                    deepCopy.add(new Server(s.getServerType(), s.getServerID(), s.getState(), s.getCurStartTime(), s.getCore(), s.getMem(), s.getDisk(), s.getWJobs(), s.getRJobs()));
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

                // Creating final variable to hold last value of least fit
                int l = leastFit;
                
                // Uses deepCopy to look for additional metrics
                //   filters servers with same cores, and in idle state
                //   if server exists, then best server becomes idle server
                deepCopy.removeIf(s -> s.getCore() - Integer.parseInt(jobSplit[4]) != l && !s.getState().contains("idle"));
                if(deepCopy.size() > 0){
                    bestServer = deepCopy.get(0);
                }

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