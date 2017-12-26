package cat.urv.imas.behaviour;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiverBehaviour extends SimpleBehaviour
{

    private MessageTemplate template;
    private long    timeOut,
            wakeupTime;
    private boolean finished;
    private int exitCode = 0;

    private ACLMessage msg;

    public ACLMessage getMessage() { return msg; }

    public ReceiverBehaviour(Agent a, int millis, MessageTemplate mt) {
        super(a);
        timeOut = millis;
        template = mt;
    }

    public void onStart() {
        reset();
        wakeupTime = (timeOut<0 ? Long.MAX_VALUE
                :System.currentTimeMillis() + timeOut);
    }

    @Override
    public int onEnd() {
        int oldExitCode = exitCode;
        reset();
        return oldExitCode;
    }

    public boolean done () {
        return finished;
    }

    public void action()
    {
        if(template == null)
            msg = myAgent.receive();
        else
            msg = myAgent.receive(template);

        if( msg != null) {
            handle( msg );
            finished = true;
            return;
        }
        long dt = wakeupTime - System.currentTimeMillis();
        if ( dt > 0 )
            block(dt);
        else {
            finished = true;
        }
    }

    public void handle( ACLMessage m) {

    }

    public void reset() {
        msg = null;
        finished = false;
        exitCode = 0;
        super.reset();
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
}
