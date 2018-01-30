package cat.urv.imas.onthology;

import java.util.HashMap;

import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;

public class DiggerOntology extends BeanOntology {
    private static final long serialVersionUID = 1L;

    // NAME
    private static final String ONTOLOGY_NAME = "digger-ontology";

    // The singleton instance of this ontology
    private static Ontology INSTANCE;

    public synchronized final static Ontology getInstance() throws BeanOntologyException {
        if (INSTANCE == null) {
            INSTANCE = new DiggerOntology();
        }
        return INSTANCE;
    }

    private DiggerOntology() throws BeanOntologyException {
        super(ONTOLOGY_NAME);

        add(InfoAgent.class);
        add(DiggerInfoAgent.class);
        add(ProspectorInfoAgent.class);
        add(RoundStart.class);
        add(DiggerTask.class);
        add(ProposeTask.class);
        add(MetalType.class);
        add(MobileAgentAction.class);
        add(MoveAction.class);
        add(CollectMetalAction.class);
        add(ReturnMetalAction.class);
        add(IdleAction.class);
        add(ActionList.class);
        add(InformAgentRound.class);
        add(DetectAction.class);
        add(InformProspectorInitialization.class);
        add(GameHasEnded.class);
    }

}


