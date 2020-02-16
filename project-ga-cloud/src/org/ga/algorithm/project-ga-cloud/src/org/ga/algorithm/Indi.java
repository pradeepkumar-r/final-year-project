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
	private Indi(Indi old) {// duplication
		// new this
		// old.Show();
		old.Cover(this);// this refer to dup new
	}

	private class Mapper {// implements Clonable{
		private int Id = 0;
		private Vm resource = null;
		private Cloudlet task = null;
		private String binString = "";
		private double procTime = 0.00;

		public Mapper(Vm k, Cloudlet s) {
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

		public Mapper Duplicate() {
			return new Mapper(this);
		}

		public void SwapWith(Mapper old) {
			Vm tmpVm = old.resource;
			old.SetVM(this.GetVM());
			this.SetVM(tmpVm);
			return;
		}

		public void SetVM(Vm k) {
			resource = k;
		}

		private Mapper(Mapper old) {
			Id = old.Id;
			resource = old.resource;
			task = old.task;
			binString = old.binString;
			procTime = old.procTime;
		}

	}// end of Mapper

	private Random random = new Random();
	private List<Mapper> mapperList = new ArrayList<Mapper>();
	private String binaryString = "";
	private Map<Integer, ArrayList<Mapper>> geneMap = new HashMap<Integer, ArrayList<Mapper>>();
	private double doneTime = 0.00;
	private double fitValue = 0.00;
	private int selfIdx = 0;
	private static int globalIdx = 0;

	// ct,vm passess to Mapper(initialization part)//check it
	public Indi(List<Vm> vmlist, List<Cloudlet> ctlist) {
		for (Cloudlet ct : ctlist) {
			int rn = random.nextInt(vmlist.size());
			Vm vm = vmlist.get(rn);
			Mapper m = new Mapper(vm, ct);
			mapperList.add(m);
		}
	}

	// key--vmid,create two dimensional matrix,see in note
	public double Evaluate() {
		binaryString = "";

		/* build genemap */
		geneMap.clear();
		ArrayList<Mapper> tmpMapperList = null;
		for (Mapper m : GetMapperList()) {
			Integer key = new Integer(m.GetVM().getId());
			if (geneMap.containsKey(key)) {
				tmpMapperList = geneMap.get(key);
				tmpMapperList.add(m);
			} else {
				tmpMapperList = new ArrayList<Mapper>();
				tmpMapperList.add(m);
				geneMap.put(key, tmpMapperList);
			}
		}
		double tmpTime = 0.00;
		double needTime = 0.00;
		doneTime = 0.00;
		for (Entry<Integer, ArrayList<Mapper>> entry : geneMap.entrySet()) {
			needTime = 0.00;
			tmpMapperList = entry.getValue();
			for (Mapper m : tmpMapperList) {
				needTime += m.Estimate();
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

	public List<Mapper> GetMapperList() {
		return mapperList;
	}

	public void Show() {
		int i = 0;
		System.out.format("## Solution[%d]: Duration=%f, Fitness=%f %n", Idx(), Duration(), Fitness());
		System.out.println(" ");

		for (Entry<Integer, ArrayList<Mapper>> entry : geneMap.entrySet()) {
			double tmpTime = 0.00;
			System.out.print("VM[" + entry.getKey() + "]: ");
			for (Mapper m : entry.getValue()) {
				tmpTime += m.GetProcTime();
				System.out.format("%04d ", m.GetCloudlet().getCloudletId());
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

	public Indi Duplicate(Indi... indvs) {
		Indi dup = null;
		if (indvs.length == 0) {
			// System.out.println("Printing when no part");
			dup = new Indi(this);
		} else {
			dup = indvs[0];
			// System.out.println("Printing duplicate indvi[0]");
			// indvs[0].Show();
			this.Cover(dup);
		}
		dup.Evaluate();
		return dup;
	}

	// Best individual cloning for new generation
	private Indi Cover(Indi newObj) {
		newObj.GetMapperList().clear();
		for (Mapper m : GetMapperList()) { // old indi Mapper
			newObj.GetMapperList().add(m.Duplicate()); // return new Mapper obj clone
		}
		// Todo: copy the geneMap
		newObj.fitValue = fitValue;
		newObj.doneTime = doneTime;
		return newObj;
	}

	public int GenIdx() {
		selfIdx = globalIdx++;
		return selfIdx;
	}

	public boolean crossoverOnePoint(Indi oppr) {
		Indi b1 = this;
		Indi b2 = oppr;
		int size = 0, p1 = 0;
		size = b1.GetMapperList().size();
		// p1 = random.nextInt(size);
		int max = size - 1;
		int min = size / 3;
		p1 = random.nextInt((max - min) + 1) + min;
		System.out.println("random integer for one point cross over" + p1);
		for (int j = 0; j <= p1; j++) {
			Mapper g1 = b1.GetMapperList().get(j);
			Mapper g2 = b2.GetMapperList().get(j);
			g1.SwapWith(g2);
		}
		System.out.println();
		// System.out.println("------------------ Before CrossOver ----------------");
		// b1.Show();
		// b2.Show();
		// System.out.println("----------------------------------------------------");
		return true;
	}

	public boolean crossoverTwoPoint(Indi oppr) {
		Indi b1 = this;
		Indi b2 = oppr;
		int size = 0, p1 = 0, p2 = 0, pp = 0;
		size = b1.GetMapperList().size();
		p1 = random.nextInt(size);
		p2 = random.nextInt(size);
		if (p1 > p2) {
			pp = p2;
			p2 = p1;
			p1 = pp;
		}

		for (int j = 0; j <= p1; j++) {
			Mapper g1 = b1.GetMapperList().get(j);
			Mapper g2 = b2.GetMapperList().get(j);
			g1.SwapWith(g2);
		}

		for (int j = p2; j < size; j++) {
			Mapper g1 = b1.GetMapperList().get(j);
			Mapper g2 = b2.GetMapperList().get(j);
			g1.SwapWith(g2);
		}
		return true;
	}

	public boolean crossoverUniformly(Indi oppr) {
		double aProba = 0.5;
		double bProba = 0.5;
		Indi b1 = this;
		Indi b2 = oppr;
		if (random.nextDouble() < aProba) {
			for (int j = 0; j < b1.GetMapperList().size(); j++) {
				if (random.nextFloat() < bProba) {
					b1.GetMapperList().get(j).SwapWith(b2.GetMapperList().get(j));
				}
			}
		}
		return true;
	}

	public List<Integer> Return() {
		List<Integer> results = new ArrayList<Integer>();
		for (Entry<Integer, ArrayList<Mapper>> entry : geneMap.entrySet()) {
			for (Mapper m : entry.getValue()) {
				results.add(new Integer(m.GetVM().getId()));
				results.add(new Integer(m.GetCloudlet().getCloudletId()));
			}
		}
		return results;
	}
}
