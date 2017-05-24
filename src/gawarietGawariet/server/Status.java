package gawarietGawariet.server;

/**
 * Created by longman on 24.05.17.
 */
public enum Status {
    LOGIN() {
        @Override
        public String toString() {
            return "Login";
        }
    },
    ERROR(){
        @Override
        public String toString() {
            return "Error";
        }
    },
    IDLE(){
        @Override
        public String toString(){
            return "Idle";
        }
    },
    SEND(){

        @Override
        public String toString(){
            return "Send";
        }
    },
    PALSELECT(){

        @Override
        public String toString(){
            return "PalSelect";
        }
    },
    LOGOUT(){

        @Override
        public String toString(){
            return "Logout";
        }
    };



    public abstract String toString();


    public static Status getStatus(String string) {
        for (Status status : Status.values()) {
            if(status.toString().equals(string))
                return status;
        }
        return ERROR;
    }

}
