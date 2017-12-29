package cat.urv.imas.behaviour;

import cat.urv.imas.agent.ImasAgent;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiverBehaviour extends SimpleBehaviour
{
    private ImasAgent agent;
    private MessageTemplate template;
    private boolean finished = false;
    private int exitCode = 0;
    boolean waitTillRoundEnd = false;

    private ACLMessage msg;

    public ACLMessage getMessage() { return msg; }

    public ReceiverBehaviour(ImasAgent a, MessageTemplate mt) {
        super(a);
        agent = a;
        template = mt;
    }

    public ReceiverBehaviour(ImasAgent a, MessageTemplate mt, boolean waitTillRoundEnd) {
        super(a);
        agent = a;
        template = mt;
        this.waitTillRoundEnd = waitTillRoundEnd;
    }

    public void onStart() {
        reset();
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
        if(waitTillRoundEnd) {
            long dt = agent.getRoundEnd() - System.currentTimeMillis();
            if ( dt > 0 )
                block(dt);
            else {
                finished = true;
            }
        } else {
            block();
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
