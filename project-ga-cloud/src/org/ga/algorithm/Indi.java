package org.ga.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class Indi {
	private class Atom {// implements Clonable{
		private int Id = 0;
		private Vm resource = null;
		private Cloudlet task = null;
		private String binString = "";
		private double procTime = 0.00;

		public Atom(Vm k, Cloudlet s) {
			resource = k;
			task = s;
			Id = s.getCloudletId();
		}

		public Vm GetVM() {
			return resource;
		}

		public Cloudlet GetCloudlet() {
			return task;
		}

		public double Estimate() {
			Vm tmpVm = GetVM();
			Cloudlet tmpCt = GetCloudlet();
			procTime = tmpCt.getCloudletLength() / (tmpVm.getMips() * tmpVm.getNumberOfPes());
			return procTime;
		}

		public double GetProcTime() {
			return procTime;
		}

	}// end of Atom

	private Random random = new Random();
	private List<Atom> atomList = new ArrayList<Atom>();
	private String binaryString = "";
	private Map<Integer, ArrayList<Atom>> geneMap = new HashMap<Integer, ArrayList<Atom>>();
	private double doneTime = 0.00;
	private double fitValue = 0.00;
	private int selfIdx = 0;

	// ct,vm passess to atom(initialization part)
	public Indi(List<Vm> vmlist, List<Cloudlet> ctlist) {
		for (Cloudlet ct : ctlist) {
			int rn = random.nextInt(vmlist.size());
			Vm vm = vmlist.get(rn);
			Atom atom = new Atom(vm, ct);
			atomList.add(atom);
		}
	}

	// key--vmid,create two dimensional matrix,see in note
	public double Evaluate() {
		binaryString = "";

		/* build genemap */
		geneMap.clear();
		ArrayList<Atom> tmpAtomList = null;
		for (Atom atom : GetAtoms()) {
			Integer key = new Integer(atom.GetVM().getId());
			if (geneMap.containsKey(key)) {
				tmpAtomList = geneMap.get(key);
				tmpAtomList.add(atom);
			} else {
				tmpAtomList = new ArrayList<Atom>();
				tmpAtomList.add(atom);
				geneMap.put(key, tmpAtomList);
			}
		}
		double tmpTime = 0.00;
		double needTime = 0.00;
		doneTime = 0.00;
		for (Entry<Integer, ArrayList<Atom>> entry : geneMap.entrySet()) {
			needTime = 0.00;
			tmpAtomList = entry.getValue();
			for (Atom atom : tmpAtomList) {
				needTime += atom.Estimate();
				/*
				 * tmpTime = gene.Estimate(); needTime = needTime>tmpTime?needTime:tmpTime;
				 */
			}
			doneTime = doneTime > needTime ? doneTime : needTime;
		}
		/* assume normal time */
		fitValue = Math.abs(doneTime - 0.00) <= 0.001 ? 1.00 : 1.00 / doneTime;
		return fitValue;
	}

	public List<Atom> GetAtoms() {
		return atomList;
	}

	public void Show() {
		int i = 0;
		System.out.format("## Solution[%d]: Duration=%f, Fitness=%f %n", Idx(), Duration(), Fitness());
		System.out.println(" ");

		for (Entry<Integer, ArrayList<Atom>> entry : geneMap.entrySet()) {
			double tmpTime = 0.00;
			System.out.print("VM[" + entry.getKey() + "]: ");
			for (Atom atom : entry.getValue()) {
				tmpTime += atom.GetProcTime();
				System.out.format("%04d ", atom.GetCloudlet().getCloudletId());
			}
			System.out.format("; Time=(%.4f)", tmpTime);
			System.out.println(" ");
		}
	}

	public int Idx() { // Key()
		return selfIdx;
	}

	public double Duration() {
		return doneTime;
	}

	public double Fitness() {
		return fitValue;
	}

}
