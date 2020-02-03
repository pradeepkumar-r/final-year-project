package org.ga.algorithm;

import java.util.ArrayList;
import java.util.List;

public class Popu {

	private Para parameters = null;
	private int currentAge = 0;
	private List<Indi> individuals = new ArrayList<Indi>();

	public Popu(Para par) {
		this.parameters = par;
	}

	public void Initialize() {
		currentAge = 0;
		// for whole population individuals are find
		for (int i = 0; i < parameters.GetPopulationSize(); i++) {
			Indi indv = new Indi(parameters.GetVMs(), parameters.GetCloudlets());
			indv.Evaluate();
			GetIndividuals().add(indv);
			System.out.println("Initialization Individual " + i);
			indv.Show();
		}

		Indi ibest = FindTheBest(GetIndividuals());
		System.out.println("#### Initial Best Solution: age=(" + currentAge + ") ####");
		System.out.println("");
		ibest.Show();
		return;
	}

	public List<Indi> GetIndividuals() {
		return individuals;
	}

	public Indi FindTheBest(List<Indi> ilist) {
		double rmax = Double.MAX_VALUE;
		Indi indv = null;
		for (Indi tmpIndv : ilist) {
			double tmpTime = tmpIndv.Duration();
			if (tmpTime < rmax) {
				indv = tmpIndv;
				rmax = tmpTime;
			}
		}
		return indv;
	}

}
