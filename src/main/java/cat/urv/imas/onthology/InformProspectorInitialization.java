package cat.urv.imas.onthology;

import java.util.ArrayList;
import java.util.List;

import jade.content.Predicate;

public class InformProspectorInitialization implements Predicate {
	
	List<Long> distances;
	
	
	public InformProspectorInitialization() {
		distances = new ArrayList<Long>();
	}
	
	public InformProspectorInitialization(List<Long> init) {
		this.distances = init;
	}

	public List<Long> getDistances() {
		return distances;
	}

	public void setDistances(List<Long> distances) {
		this.distances = distances;
	}
	
	
	
}
