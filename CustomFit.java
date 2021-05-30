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
        while(!job.contains("NONE")){

            // If message contains JOBN
            if (job.contains("JOBN")){
                // Split message to
                // jobn | sub_time | job_id | estimated_time | cores | mem | disk
                String[] jobSplit = job.split("\\s");

                // Request Capable servers
                c.sendMsg("GETS Capable " + jobSplit[4] + " " + jobSplit[5] + " " + jobSplit[6]);
                c.readMsg();
                c.sendMsg("OK");

                // Split server String
                String server = c.readMsg();
                
                c.sendMsg("OK");
                c.readMsg();

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

                ArrayList<Server> deepCopy = new ArrayList<Server>();

                for (Server s: serverList){
                    deepCopy.add(new Server(s.getServerType(), s.getServerID(), s.getState(), s.getCurStartTime(), s.getCore(), s.getMem(), s.getDisk(), s.getWJobs(), s.getRJobs()));
                }

                // deepCopy.removeIf(s -> s.getRJobs() > 0 && s.getWJobs() == 0 && (s.getCore() - Integer.parseInt(jobSplit[4]) > 0));
                
                // if (deepCopy.size() > 0){
                //     serverList.removeIf(s -> s.getRJobs() > 0 && s.getWJobs() == 0 && (s.getCore() - Integer.parseInt(jobSplit[4]) > 0));
                // }

                // Least fitness value
                //  Starts with first server
                int leastFit = serverList.get(0).getCore() - Integer.parseInt(jobSplit[4]);
                // Server with least fitness value
                Server bestServer = serverList.get(0);
                


                // For each server, find fitness (server core - job cores),
                // if current server fitness value is smaller than current server OR server has less waiting jobs
                //   leastFit becomes current server / hold new best server          
                for(Server i : serverList){                    
                    int fitnessValue = i.getCore() - Integer.parseInt(jobSplit[4]);
                    if (leastFit < 0){
                        leastFit = fitnessValue;
                        bestServer = i;
                    } else if ((fitnessValue < leastFit) && i.getWJobs() < bestServer.getWJobs()){
                        leastFit = fitnessValue;
                        bestServer = i;
                    }
                    
                }
                int l = leastFit;
                deepCopy.removeIf(s -> s.getCore() - Integer.parseInt(jobSplit[4]) != l);
                
                Integer leastWait = null;
                Server leastServer = null;
                for (Server s: deepCopy){
                    c.sendMsg("LSTJ " + s.getServerType() + " " + s.getServerID());
                    c.readMsg();
                    c.sendMsg("OK");
                    String check = c.readMsg();
                    
                    Integer rWait = 0;
                    Integer wWait = 0;
                    while(!check.contains(".")){
                        c.sendMsg("OK");
                        check = c.readMsg();
                        String[] servJobSplit = check.split("\\s");
                        if (servJobSplit.length >= 7){
                            if (Integer.parseInt(servJobSplit[1]) == 1){
                                wWait += Integer.parseInt(servJobSplit[4]);
                            } else {
                                rWait += Integer.parseInt(servJobSplit[4]);
                            }
                        }
                        
                    }
                    if (leastWait == null){
                        if (wWait > 0){
                            leastWait = wWait;
                            leastServer = s;
                        } else {
                            leastWait = rWait;
                            leastServer = s;
                        }
                    } else {
                        if (wWait > 0 && wWait < leastWait){
                            wWait = leastWait;
                            leastServer = s;
                        } else if (rWait < leastWait){
                            leastWait = rWait;
                            leastServer = s;
                        }
                    }
                }


                c.sendMsg("SCHD " + jobSplit[2] + " " + bestServer.getServerType() + " " + bestServer.getServerID());
                c.readMsg();
            }

            c.sendMsg("REDY");
            job = c.readMsg();
        }

    }
}