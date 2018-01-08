package cat.urv.imas.onthology;

import jade.content.Predicate;

import java.util.ArrayList;
import java.util.List;

import cat.urv.imas.map.FieldCell;

public class FoundMetalsList implements Predicate {
    private List<FieldCell> foundMetalsList;

    public FoundMetalsList() {
    	this.foundMetalsList = new ArrayList<FieldCell>();
    }

    public FoundMetalsList(List<FieldCell> foundMetalsList) {
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
