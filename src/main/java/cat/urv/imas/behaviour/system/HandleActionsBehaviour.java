package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

public class HandleActionsBehaviour extends SimpleBehaviour
{

    private MessageTemplate template;
    private long    timeOut,
            wakeupTime;
    private boolean finished;

    private ACLMessage msg;

    public ACLMessage getMessage() { return msg; }

    private SystemAgent systemAgent;


    public HandleActionsBehaviour(SystemAgent a) {
        super(a);
        systemAgent = a;
        timeOut = 30000;
        template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    }

    public void onStart() {
        reset();
        systemAgent.log("Waiting for actions for round "+systemAgent.getCurrentRound());
        wakeupTime = (timeOut<0 ? Long.MAX_VALUE
                :System.currentTimeMillis() + timeOut);
    }

    @Override
    public int onEnd() {
        reset();
        return 0;
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
        // TODO save actions for this round for next advancing
        systemAgent.log("I received the actions from the coordinator.");
    }

    public void reset() {
        msg = null;
        finished = false;
        super.reset();
    }

}
