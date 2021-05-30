class Client {
    public static void main(String args[]) throws Exception {
        Protocol p = Protocol.getInstance();
        Communication c = new Communication(p.din, p.dout);

        // Start handshake
        c.sendMsg("HELO");
        c.readMsg();
        c.sendMsg("AUTH " + System.getProperty("user.name"));
        c.readMsg();

        // Read command line arguments
        String alg = new String();
        if (args.length > 0){
            alg = args[0];
        } else {
            System.out.println("Please select an algorithm '-cf / -bf / -hf / -pi'");
            System.exit(1);
        }
        
        // Choose Algorithm
        if (alg.equals("-cf")){
            CustomFit f = new CustomFit(c);
            f.runCustomFit();
        } else if (alg.equals("-bf")){
            BestFit f = new BestFit(c);
            f.runBestFit();
        } else if (alg.equals("-pi")){
            PrioritiseIdle f = new PrioritiseIdle(c);
            f.runPrioritiseIdle();
        }
        else if (alg.equals("-hf")){
            HalfFit f = new HalfFit(c);
            f.runHalfFit();
        }
        else {
            System.out.println("Algorithm doesn't exist. Please select an algorithm '-cf / -bf / -hf / -pi'");
            System.exit(1);
        }
        
        c.sendMsg("QUIT");
        
        p.close();
    }

}