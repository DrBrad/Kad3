package unet.kad3.routing;

import unet.kad3.routing.KB.KRoutingTable;
import unet.kad3.routing.mainline_todo.MRoutingTable;

public enum BucketTypes {

    MAINLINE {
        @Override
        public String value(){
            return "MainLine";
        }
        @Override
        public Class<?> getRoutingTable(){
            return MRoutingTable.class;
        }
    }, KADEMLIA {
        @Override
        public String value(){
            return "Kademlia";
        }
        @Override
        public Class<?> getRoutingTable(){
            return KRoutingTable.class;
        }
    };

    public String value(){
        return null;
    }

    public Class<?> getRoutingTable(){
        return null;
    }
}
