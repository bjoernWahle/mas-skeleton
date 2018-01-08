package cat.urv.imas.onthology;

import java.util.ArrayList;
import java.util.List;

import cat.urv.imas.map.FieldCell;
import jade.content.Predicate;

public class InformProspector extends InformAgentAction {
	private List<FieldCell> foundMetalsList;

    public InformProspector() {
    	super();
    	this.foundMetalsList = new ArrayList<FieldCell>();
    }
    
    public InformProspector(MobileAgentAction action,List<FieldCell> foundMetalsList) {
    	super(action);
    	if(foundMetalsList != null) {
    		this.foundMetalsList = foundMetalsList;
    	}else {
    		this.foundMetalsList = new ArrayList<FieldCell>();
    	}
        
    }

    public List<FieldCell> getFoundMetalsList() {
        return foundMetalsList;
    }

    public void setAgentList(List<FieldCell> foundMetalsList) {
        this.foundMetalsList = foundMetalsList;
    }
    
    public void addFoundMetals(List<FieldCell> foundMetalsList) {
    	this.foundMetalsList.addAll(foundMetalsList);
    }
    
    public boolean anyElements() {
    	return !foundMetalsList.isEmpty();
    }
}
