package org.ga.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Popu {

	private Para parameters = null;
	private int currentAge = 0;
	private List<Indi> individuals = new ArrayList<Indi>();
	private Random random = new Random();
	private List<Indi> indvSelects = new ArrayList<Indi>();
	private double[] indvFitnessPortions = null;
	private int replacement = 0;

	public Popu(Para par) {
		this.parameters = par;
	}

	public void Initialize() {
		currentAge = 0;
		// Initialization part starts
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
		System.out.println("----------------------------------------------------------------");
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

	public Popu Evolve() {
		int i = 0;

		Popu nextpop = new Popu(parameters);
		Indi indvAdd = null;

		/* for special test */
		int currentOpt = parameters.GetOption(); // get from xml ga

		// if (currentOpt == 5) {
		indvAdd = FindTheBest(GetIndividuals()).Duplicate();// copyiny best individual
		nextpop.increase(indvAdd);// add best one
		// }
		System.out.println("The best one from pre gen is added to next gen");
		indvAdd.Show();
		System.out.println("---------------------------------------------------------------");
		// if (parameters.GetSelectionPolicy() == "Roulette-Wheel") {
		// GenFitnessPortions();
		// }

		int totalSize = 0;
		while (totalSize < parameters.GetPopulationSize()) {
			indvAdd = null;
			Selection();
			/* for special test */
			if (currentOpt == 5) {
				System.out.println("------------------- Selected Individual for crossover -------------------------");
				for (Indi indv : GetSelecteds()) {
					indvAdd = indv.Duplicate();// avoid reference
					nextpop.increase(indvAdd);
					indv.Show();
				}
				System.out.println("------------------------------------------------------------------");
			}
			System.out.println("THE next popu size is" + nextpop.individuals.size());
			// System.out.println(nextpop.show);

			Crossover();
			// Mutation();
			System.out.println("-------------------After CrossOver Evaluation-----------------");
			for (Indi indv : GetSelecteds()) {
				indv.Evaluate();
				// nextpop.increase(GetSelecteds());
				nextpop.increase(indv.Duplicate());
				indv.Show();
			}
			System.out.println("---------------------------------------------------------------");
			// Replace();
			// if (currentOpt == 2) {
			// Replace();
			// }
			/*
			 * special indvAdd = FindTheBest(GetSelecteds()).Duplicate();
			 * nextpop.increase(indvAdd); totalSize+=1;
			 */
			// totalSize += GetSelecteds().size();
			totalSize = nextpop.individuals.size();
			System.out.println(nextpop.individuals.size());
		}
		// nextpop.SetAge(++i);
		System.out.println("--------------------next popu generated is-------------------------");
		for (Indi in : nextpop.GetIndividuals()) {
			in.Show();
		}
		System.out.println("---------------------------------------------------------------------");
		return nextpop;
	}

	private Popu increase(Indi... indvss) {
		for (Indi tmpIndv : indvss) {
			tmpIndv.GenIdx();
			GetIndividuals().add(tmpIndv);
		}
		return this;
	}

	private Popu increase(List<Indi> indvlist) {
		for (Indi tmpIndv : indvlist) {
			tmpIndv.GenIdx();
			GetIndividuals().add(tmpIndv);
		}
		return this;
	}

	private void GenFitnessPortions() {
		double totFitvalue = 0.00;
		int curSize = GetIndividuals().size();
		indvFitnessPortions = new double[curSize + 1];
		for (Indi indv : GetIndividuals()) {
			totFitvalue += indv.Fitness();
		}
		indvFitnessPortions[0] = 0.00;

		for (int i = 1; i <= curSize; i++) {
			indvFitnessPortions[i] = indvFitnessPortions[i - 1] + (GetIndividuals().get(i - 1).Fitness() / totFitvalue);
		}

		double lastProba = indvFitnessPortions[curSize];
		if (Math.abs(lastProba - 1.0) > 0.0001) {// TODO: preciseness
			System.out.println("#ERROR: The sum of probabilities is not 1.00 but " + lastProba);// TODO: throw new
																								// Exception("");
		}
	}

	// public List<Indi> Selection() {
	// Indi ib1st = null;
	// Indi ib2nd = null;
	// GetSelecteds().clear();
	//
	// switch (parameters.GetSelectionPolicy()) {
	// case "Randomly":
	// int rn = 0;
	// rn = random.nextInt(GetIndividuals().size());
	// ib1st = GetIndividuals().get(rn);
	// rn = random.nextInt(GetIndividuals().size());
	// ib2nd = GetIndividuals().get(rn);
	// break;
	// case "Best-Two":
	// ib1st = FindTheBest(GetIndividuals());
	// ib2nd = FindSndBest(GetIndividuals());
	// break;
	// case "Roulette-Wheel":
	// ib1st = selectionRouletteWheel();
	// ib2nd = selectionRouletteWheel();
	// break;
	// case "Tournament":
	// ib1st = selectionTournament(1);// select first one
	// while (true) {
	// ib2nd = selectionTournament(2);// select second one
	// if (ib1st.Duration() != ib2nd.Duration())
	// break;
	// }
	// break;
	// default:
	// System.out.println("#ERROR: no such selection policy (" +
	// parameters.GetSelectionPolicy() + ")");
	// return null;
	// }
	//
	// ib1st = ib1st.Duplicate();
	// ib2nd = ib2nd.Duplicate();
	// indvSelects.add(ib1st);
	// indvSelects.add(ib2nd);
	// return GetSelecteds();
	// }
	public List<Indi> Selection() {
		Indi ib1st = null;
		Indi ib2nd = null;
		GetSelecteds().clear();

		switch (parameters.GetSelectionPolicy()) {
		case "Randomly":
			int rn = 0;
			rn = random.nextInt(GetIndividuals().size());
			ib1st = GetIndividuals().get(rn);
			rn = random.nextInt(GetIndividuals().size());
			ib2nd = GetIndividuals().get(rn);
			break;
		case "Best-Two":
			ib1st = FindTheBest(GetIndividuals());
			ib2nd = FindSndBest(GetIndividuals());
			break;
		case "Roulette-Wheel":
			ib1st = selectionRouletteWheel();
			ib2nd = selectionRouletteWheel();
			break;
		case "Tournament":
			ib1st = selectionTournament();
			while (true) {
				ib2nd = selectionTournament();
				if (ib1st.Duration() != ib2nd.Duration())
					break;
			}
			break;
		default:
			System.out.println("#ERROR: no such selection policy (" + parameters.GetSelectionPolicy() + ")");
			return null;
		}

		ib1st = ib1st.Duplicate();
		ib2nd = ib2nd.Duplicate();
		indvSelects.add(ib1st);
		indvSelects.add(ib2nd);
		return GetSelecteds();
	}

	public List<Indi> GetSelecteds() {
		return indvSelects;
	}
	//
	// public Indi FindSndBest(List<Indi> ilist) {
	// int b1 = 0;
	// int b2 = 0;
	// for (int i = 0; i < ilist.size(); i++) {
	// if (ilist.get(i).Fitness() > ilist.get(b1).Fitness()) {
	// b2 = b1;
	// b1 = i;
	// } else if (ilist.get(i).Fitness() > ilist.get(b2).Fitness()) {
	// b2 = i;
	// }
	// }
	// return ilist.get(b2);
	// }

	public Indi FindSndBest(List<Indi> ilist, Indi ibest) {
		Indi sbest = null;
		double rmax = Double.MAX_VALUE;
		// Indi indv = null;
		for (Indi tmpIndv : ilist) {
			if (tmpIndv != ibest) {
				double tmpTime = tmpIndv.Duration();
				if (tmpTime < rmax) {
					sbest = tmpIndv;
					rmax = tmpTime;
				}
			}
		}
		return sbest;
	}

	public Indi FindSndBest(List<Indi> ilist) {
		Indi sbest = null;
		Indi fbest = null;
		fbest = FindTheBest(ilist);
		ilist.remove(fbest);
		fbest = FindTheBest(ilist);
		sbest = fbest;
		return sbest;
	}

	private Indi selectionRouletteWheel() {

		/* TODO: add replacement to control */
		if (replacement != 0) {
			GenFitnessPortions();
		}

		Indi ibest = null;
		double ranProba = random.nextDouble();
		int curSize = GetIndividuals().size();
		for (int k = 1; k <= curSize; k++) {
			if (ranProba >= indvFitnessPortions[k - 1] && ranProba <= indvFitnessPortions[k]) {
				ibest = GetIndividuals().get(k - 1);
				break;
			}
		}
		return ibest;
	}

	// private Indi selectionTournament(int i) {
	// // tournament selection
	// Indi ibest = null;
	// Indi icheck = null;
	// int rn = 0;
	//
	// /*
	// * for particular test if(currentOpt==5){ parameters.SetTournamentRate(0.75);
	// * parameters.SetTournamentSize(2); }
	// */
	// List<Indi> tournamentIndvs = new ArrayList<Indi>();
	// for (int j = 0; j < parameters.GetTournamentSize();) {
	// rn = random.nextInt(GetIndividuals().size());
	// // icheck = GetIndividuals().get(rn);
	// // if (icheck != null && !tournamentIndvs.contains(icheck))// code for not
	// // duplicating
	// // tournamentIndvs.add(icheck);
	// if (icheck == null) {
	// icheck = GetIndividuals().get(rn);
	// tournamentIndvs.add(icheck);
	// j++;
	// } else if (!tournamentIndvs.contains(GetIndividuals().get(rn))) {
	// icheck = GetIndividuals().get(rn);
	// tournamentIndvs.add(icheck);
	// j++;
	// }
	// }
	// double ppp = random.nextDouble();
	// if (i == 1) {
	// ibest = FindTheBest(tournamentIndvs);
	// } else {
	// ibest = FindTheBest(tournamentIndvs);
	// ibest = FindSndBest(tournamentIndvs, ibest);
	// }
	// System.out.println("Selected one is " + i);
	// ibest.Show();
	// System.out.println("-------------------------------");
	// return ibest;
	// }
	private Indi selectionTournament() {// tournament selection
		Indi ibest = null;
		Indi icheck = null;
		int rn = 0;

		/*
		 * for particular test if(currentOpt==5){ parameters.SetTournamentRate(0.75);
		 * parameters.SetTournamentSize(2); }
		 */
		List<Indi> tournamentIndvs = new ArrayList<Indi>();
		ibest = null;
		for (int j = 0; j < parameters.GetTournamentSize(); j++) {
			rn = random.nextInt(GetIndividuals().size());
			icheck = GetIndividuals().get(rn);
			tournamentIndvs.add(icheck);
		}
		double ppp = random.nextDouble();
		if (ppp < parameters.GetTournamentRate()) {
			ibest = FindTheBest(tournamentIndvs);
		} else {
			ibest = FindSndBest(tournamentIndvs);
		}
		return ibest;
	}

	public List<Indi> Crossover() {
		double iproba = 1.00;
		int isize = GetSelecteds().size();

		if (isize < 2) {
			System.out.println("#ERROR: nothing to be crossover");
			return GetSelecteds();
		}

		// for (int i = 0; i < isize; i += 2) {
		// if (parameters.GetCrossoverRate() < random.nextDouble()) {
		// continue;
		// }
		// Indi b1 = GetSelecteds().get(i);
		// Indi b2 = GetSelecteds().get(i + 1);
		Indi b1 = GetSelecteds().get(0);
		Indi b2 = GetSelecteds().get(1);
		switch (parameters.GetCrossoverPolicy()) {
		case "One-Point":
			b1.crossoverOnePoint(b2);
			break;
		case "Two-Point":
			b1.crossoverTwoPoint(b2);
			break;
		case "Uniformly":
			b1.crossoverUniformly(b2);
			break;
		default:
			break;

		}
		// System.out.println("After crossover pudushu");
		// b1.Show();
		// b2.Show();
		return GetSelecteds();
	}

}
