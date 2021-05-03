public class Server {
    String serverType;
    int serverID;
    String state;
    int curStartTime;
    int core;
    int mem;
    int disk;
    int wJobs;
    int rJobs;

    public Server(String serverType, int serverID, String state, int curStartTime, int core, int mem, int disk, int wJobs, int rJobs){
        this.serverType = serverType;
        this.serverID = serverID;
        this.state = state;
        this.curStartTime = curStartTime;
        this.core = core;
        this.mem = mem;
        this.disk = disk;
        this.wJobs = wJobs;
        this.rJobs = rJobs;
    }

    public Server(){};

    public String getServerType(){
        return this.serverType;
    }
    public int getServerID(){
        return this.serverID;
    }
    public String getState(){
        return this.state;
    }
    public int getCurStartTime(){
        return this.curStartTime;
    }
    public int getCore(){
        return this.core;
    }
    public int getMem(){
        return this.mem;
    }
    public int getDisk(){
        return this.disk;
    }
    public int getWJobs(){
        return this.wJobs;
    }
    public int getRJobs(){
        return this.rJobs;
    }

    public void setServerType(String serverType){
        this.serverType = serverType;
    }
    public void setServerID(int serverID){
        this.serverID = serverID;
    }
    public void setState(String state){
        this.state = state;
    }
    public void setCurStartTime(int curStartTime){
        this.curStartTime = curStartTime;
    }
    public void setCore(int core){
         this.core = core;
    }
    public void setMem(int mem){
         this.mem = mem;
    }
    public void setDisk(int disk){
         this.disk = disk;
    }
    public void setWJobs(int wJobs){
         this.wJobs = wJobs;
    }
    public void setRJobs(int rJobs){
         this.rJobs = rJobs;
    }
}
