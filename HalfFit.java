import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class HalfFit {
    Communication c;

    public HalfFit(Communication c){
        this.c = c;
    }
    public void runHalfFit() throws IOException{

        c.sendMsg("REDY");
        String job = c.readMsg();
        Boolean firstJob = false;

        c.sendMsg("GETS All");
        c.readMsg();
        c.sendMsg("OK");
        String half = c.readMsg();
        String[] halfSplit = half.split("\\s");

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

        int largest = largestCore;
        String type = largestType;
        halfList.removeIf(s -> !s.getServerType().contains(type));


        while(!job.contains("NONE")){

            // If message contains JOBN
            if (job.contains("JOBN")){

                // Split message to
                // jobn | sub_time | job_id | estimated_time | cores | mem | disk
                String[] jobSplit = job.split("\\s");

                c.sendMsg("GETS Capable " + jobSplit[4] + " " + jobSplit[5] + " " + jobSplit[6]);
                c.readMsg();
                c.sendMsg("OK");
                String server = c.readMsg();
                String[] serverSplit = server.split("\\s");

                // Split server String
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

                c.sendMsg("OK");
                c.readMsg();

                if (Integer.parseInt(jobSplit[4]) > secondLargest){
                    
                    serverList.removeIf(s -> !s.getServerType().contains(type));
                    
                    Boolean finished = false;
                    Server bestServer = serverList.get(0);
                    for(Server s: serverList){
                        if (bestServer.getState().contains("unavailable")) {
                            bestServer = s; 
                        } else if (s.getState().contains("idle") && finished == false){
                            bestServer = s;
                            finished = true;
                        } else if (s.getState().contains("inactive") && finished == false){
                            bestServer = s;
                            finished = true;
                        } else if (s.getState().contains("active") && finished == false){
                            if (bestServer.getWJobs() >= s.getWJobs()){
                                bestServer = s;
                            }
                        }
                    }
                    c.sendMsg("SCHD " + jobSplit[2] + " " + bestServer.getServerType() + " " + bestServer.getServerID());
                    c.readMsg();

                } else {
                    // Split server String
                    // Create list of servers
    
                    for(Server s: serverList){
                        for(Server h: halfList){
                            if(s.getServerType().contains(h.getServerType()) && (!s.getState().contains("active") || !s.getState().contains("idle"))){
                                serverList.remove(h);
                            }
                        }
                    }

                    ArrayList<Server> deepCopy = new ArrayList<Server>();

                    for (Server s: serverList){
                        deepCopy.add(new Server(s.getServerType(), s.getServerID(), s.getState(), s.getCurStartTime(), s.getCore(), s.getMem(), s.getDisk(), s.getWJobs(), s.getRJobs()));
                    }

                    deepCopy.removeIf(s -> !s.getState().contains("active") && !s.getState().contains("booting") && !s.getState().contains("idle"));

                    int largestFit = serverList.get(0).getCore() - Integer.parseInt(jobSplit[4]);
                    Server bestServer = serverList.get(0);

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
                    } else {
                        for(Server s: serverList){
                            int fitnessValue = s.getCore() - Integer.parseInt(jobSplit[4]);
                            if (largestFit < fitnessValue){
                                largestFit = fitnessValue;
                                bestServer = s;
                            }
                        }
                    }

                    c.sendMsg("SCHD " + jobSplit[2] + " " + bestServer.getServerType() + " " + bestServer.getServerID());
                    c.readMsg();
                }

                
            }

            c.sendMsg("REDY");
            job = c.readMsg();
        }

    }
}