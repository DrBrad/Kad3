package unet.kad3;

import unet.kad3.kad.Kademlia;

public class Main {

    /*
     * LOCAL VS. EXTERNAL
     *
     * WE WILL ALWAYS ACCEPT ANYTHING THAT HAS A VERIFIED ID AND IS PINGABLE
     *
     * - NOTE
     *     THIS MEANS THAT I CAN ADD A LOCAL NODE BUT ONLY ON A LOCAL DHT - IF THE MAJORITY OF THE NODES ARE EXTERNAL I
     *     CANNOT ADD THE NODE AS THE ID WILL BE INVALID FOR THE LOCAL NODE
     *
     *     EXAMPLE
     *       I192.168.0.7 E96.97.98.99 -> PINGs 192.168.0.8
     *       I192.168.0.8 E96.97.98.99 WILL NOT ADD TO BUCKET
     *
     *     FACTS
     *       WE WILL NEVER RECEIVE A LOCAL IP OF A NODE ON OUR NETWORK FROM AN EXTERNAL NODE
     *
     *       EXAMPLE
     *         E96.97.98.99 Will never receive a packet from E96.97.96.97 about one of our local I192.168.0.???
     *
     *     IT MIGHT BE SMART THAT IF WE FIND_NODEs ON AN EXTERNAL NODE NOT TO EVEN TRY PINGING OR VERIFYING LOCAL_NODES
     *
     */

    //REMEMBER THREAD POOL...

    //WOULD IT BE BETTER TO NOT USE POOLS BUT JUST TAKE EACH PACKET AND SEND IT TO A NEW THREAD...?

    public static void main(String[] args){
        try{
            Kademlia k = new Kademlia("Kademlia", 8080);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
