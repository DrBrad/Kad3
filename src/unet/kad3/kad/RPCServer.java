package unet.kad3.kad;

import unet.kad3.messages.PingRequest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RPCServer {

    private DatagramSocket server;
    private ConcurrentLinkedQueue<DatagramPacket> packetPool = new ConcurrentLinkedQueue<>();

    public RPCServer(int port){
        try{
            server = new DatagramSocket(port);

            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(!server.isClosed()){
                        try{
                            DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                            server.receive(packet);

                            if(packet != null){
                                packetPool.offer(packet);
                            }
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();


            new Thread(new Runnable(){
                @Override
                public void run(){
                    while(!server.isClosed()){
                        if(!packetPool.isEmpty()){
                            DatagramPacket packet = packetPool.poll();

                            //MessageBase b = new MessageBase(packet.getData());

                            //DECODE PACKET TO BENCODE - SEND OFF TO LISTENER

                            /*
                            try{

                            }catch(IOException e){
                                e.printStackTrace();
                            }
                            */
                        }
                    }
                }
            }).start();

        }catch(IOException e){
            e.printStackTrace();
        }
    }


    //WE REALLY JUST NEED TO FIGURE OUT IF HE IS EVEN TAKING INTO ACCOUNT THE PACKETS ORIGIN IP:PORT OR NOT...
    public void ping(InetAddress address, int port){
        PingRequest pr = new PingRequest();
        //pr.setUID(deriveID);
        //pr.setTransactionID(); //THIS IS IMPORTANT
        pr.setDestination(address, port);
        //SET IP AS MY PUBLIC IP...
        //SET MY PORT

        //SEND THIS SHIT...
    }

    public int getPort(){
        return server.getLocalPort();
    }

    public void start(){

    }

    public void stop(){

    }
}
