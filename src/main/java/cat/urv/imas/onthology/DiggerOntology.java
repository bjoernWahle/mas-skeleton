package cat.urv.imas.onthology;

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
        add(MetalType.class);
        add(MobileAgentAction.class);
        add(MoveAction.class);
        add(IdleAction.class);
        add(ActionList.class);
        add(InformAgentAction.class);
        add(InformProspector.class);
    }

}


