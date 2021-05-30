import java.io.*;
import java.net.*;
import java.util.ArrayList;

// Half Fit implementation
public class HalfFit {
    Communication c;

    // Half fit requires singleton instance communication
    public HalfFit(Communication c){
        this.c = c;
    }

    // throws IOException inorder to use socket, din, dout
    public void runHalfFit() throws IOException{

        // Send first ready
        c.sendMsg("REDY");
        String job = c.readMsg();

        // Grab list of all servers
        c.sendMsg("GETS All");
        c.readMsg();
        c.sendMsg("OK");
        String half = c.readMsg();
        String[] halfSplit = half.split("\\s");

        // Create arraylist of all servers
        ArrayList<Server> halfList = new ArrayList<Server>();
        for (int i = 0; i < halfSplit.length; i += 9){
            if ((halfSplit.length - i) >= 9){
                Server s = new Server();
                    s.setServerType(halfSplit[i]);
                    s.setServerID(Integer.parseInt(halfSplit[i+1]));                        
                    s.setState(halfSplit[i+2]);
                    s.setCurStartTime(Integer.parseInt(halfSplit[i+3]));
                    s.setCore(Integer.parseInt(halfSplit[i+4]));
                    s.setMem(Integer.parseInt(halfSplit[i+5]));
                    s.setDisk(Integer.parseInt(halfSplit[i+6]));
                    s.setWJobs(Integer.parseInt(halfSplit[i+7]));
                    s.setRJobs(Integer.parseInt(halfSplit[i+8]));
                    halfList.add(s);
            }
        }
        c.sendMsg("OK");
        c.readMsg();

        // Looks for largest and second largest server based on cores
        int largestCore = 0;
        int secondLargest = 0;
        String largestType = "";
        for(Server s: halfList){
            if (s.getCore() > largestCore){
                secondLargest = largestCore;
                largestCore = s.getCore();
                largestType = s.getServerType();
            }
        }

        // Half list only contains servers of largest cores
        String type = largestType;
        halfList.removeIf(s -> !s.getServerType().contains(type));


        // If the response is not none
        while(!job.contains("NONE")){

            // If message indicates there is a job
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

                c.sendMsg("OK");
                c.readMsg();

                // If the job cores can only be performed by largest server
                if (Integer.parseInt(jobSplit[4]) > secondLargest){
                    
                    // Removes any server that is not the largest type of server
                    serverList.removeIf(s -> !s.getServerType().contains(type));
                    
                    // Look through all servers, looks through different states to find servers
                    //   eventually finds server that can effectively start the job the soonest
                    // Boolean finished ensures that algorithm does not keep going when server with higher priority is found
                    Boolean finished = false;
                    Server bestServer = serverList.get(0);
                    for(Server s: serverList){
                        // If best server is unavailable, best server becomes current server
                        if (bestServer.getState().contains("unavailable")) {
                            bestServer = s; 
                        // If server is idle, then this server has maximum priority
                        } else if (s.getState().contains("idle") && finished == false){
                            bestServer = s;
                            finished = true;
                        // If there is an inactive server, then takes second priorty
                        } else if (s.getState().contains("inactive") && finished == false){
                            bestServer = s;
                            finished = true;
                        // Otherwise find all active servers and find ones with least waiting jobs
                        } else if (s.getState().contains("active") && finished == false){
                            if (bestServer.getWJobs() >= s.getWJobs()){
                                bestServer = s;
                            }
                        }
                    }
                    // Schedule best server
                    c.sendMsg("SCHD " + jobSplit[2] + " " + bestServer.getServerType() + " " + bestServer.getServerID());
                    c.readMsg();

                } else {
                    
                    // Traverse through list of capable servers
                    //   remove all the largest servers such that they cannot be used by smaller jobs
                    //   only keeps largest servers that are active (incase we can allocate more jobs for efficiency)
                    for(Server s: serverList){
                        for(Server h: halfList){
                            if(s.getServerType().contains(h.getServerType()) && (!s.getState().contains("active") || !s.getState().contains("idle"))){
                                serverList.remove(h);
                            }
                        }
                    }

                    // Create deepcopy array of server list
                    ArrayList<Server> deepCopy = new ArrayList<Server>();

                    for (Server s: serverList){
                        deepCopy.add(new Server(s.getServerType(), s.getServerID(), s.getState(), s.getCurStartTime(), s.getCore(), s.getMem(), s.getDisk(), s.getWJobs(), s.getRJobs()));
                    }

                    // Keep all servers that are active, booting, idle
                    //   (if it isn't active, booting, idle, then remove them)
                    deepCopy.removeIf(s -> !s.getState().contains("active") && !s.getState().contains("booting") && !s.getState().contains("idle"));

                    // Largest fitness value
                    //   initialising as first server fitness value
                    int largestFit = serverList.get(0).getCore() - Integer.parseInt(jobSplit[4]);
                    // Server with least fitness value
                    Server bestServer = serverList.get(0);

                    // Look through all servers, looks through different states to find servers
                    //   eventually finds server that can effectively start the job the soonest
                    if (deepCopy.size() > 0){
                        Boolean finished = false;
                        for(Server s: deepCopy){
                        if (bestServer.getState().contains("unavailable")) {
                            bestServer = s; 
                        } else if (s.getState().contains("idle") && finished == false){
                            bestServer = s;
                            finished = true;
                        } else if (s.getState().contains("active") && finished == false){
                            if (bestServer.getWJobs() >= s.getWJobs()){
                                bestServer = s;
                            }
                        } else if (s.getState().contains("inactive") && finished == false){
                            bestServer = s;
                            finished = true;
                        }
                    }

                    // If no servers fit criteria then look for largest fitness value
                    } else {
                        for(Server s: serverList){
                            int fitnessValue = s.getCore() - Integer.parseInt(jobSplit[4]);
                            if (largestFit < fitnessValue){
                                largestFit = fitnessValue;
                                bestServer = s;
                            }
                        }
                    }

                    // Schedule job with best matching server
                    c.sendMsg("SCHD " + jobSplit[2] + " " + bestServer.getServerType() + " " + bestServer.getServerID());
                    c.readMsg();
                }
            }

            // Send server client is REDY for next job
            c.sendMsg("REDY");
            job = c.readMsg();
        }

    }
}