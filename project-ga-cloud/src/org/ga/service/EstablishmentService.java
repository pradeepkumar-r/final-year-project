package org.ga.service;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class EstablishmentService {

	private Document docConfig = null; // Document in dom4j
	public List<Datacenter> datacenters = null;
	public List<DatacenterBroker> datacenterbrokers = null;

	private static int totalHosts = 0;
	private static int totalVMs = 0;
	private static int totalCloudlets = 0;

	public EstablishmentService(String filePath) { // filePath is xml file name
		try {
			parseConfigFile(filePath);

			datacenters = createDatacenters();
			datacenterbrokers = createDatacenterBrokers();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private void parseConfigFile(String filePath) throws DocumentException {
		File inFile = new File(filePath);
		SAXReader reader = new SAXReader(); // SaxReader in domj4 used for xml reading
		docConfig = reader.read(inFile);
	}

	public List<Datacenter> createDatacenters() { // Node in dom4j

		List<Node> nodeList = docConfig.selectNodes("//establishment/datacenter[@valid='true']");
		List<Datacenter> dcArray = new ArrayList<Datacenter>();

		int i = 0;
		nodeList.stream().forEach(node -> {
			dcArray.add(getDatacenter(node)); // add Datacenter objects
		});

		// List<Datacenter> dummy = nodeList.stream().map(node -> {
		// return createDatacenter(node);
		// }).collect(Collectors.toList());
		// for (Iterator<Node> iter = nodeList.iterator(); iter.hasNext();) {
		// Node node = iter.next();
		// dcArray.add(createDatacenter(node));
		// }
		return dcArray;
	}

	public Datacenter getDatacenter(Node current) {

		// Getting data from xml file
		String name = current.selectSingleNode("name").getText();
		String arch = current.selectSingleNode("arch").getText();
		String os = current.selectSingleNode("os").getText();
		int count = Integer.parseInt(current.selectSingleNode("count").getText());
		String vmm = current.selectSingleNode("vmm").getText();
		double timezone = Double.parseDouble(current.selectSingleNode("timezone").getText());
		double costPerProc = Double.parseDouble(current.selectSingleNode("cost/proc").getText());
		double costPerMem = Double.parseDouble(current.selectSingleNode("cost/memory").getText());
		double costPerStorage = Double.parseDouble(current.selectSingleNode("cost/storage").getText());
		double costPerBw = Double.parseDouble(current.selectSingleNode("cost/bandwidth").getText());
		String policyName = current.selectSingleNode("policy").getText();

		List<Storage> storageList = new ArrayList<Storage>(); // Storage in cloudsim
		List<Host> hostList = new ArrayList<Host>(); // Host
		List<Node> nodeList = current.selectNodes("hosts[@valid='true']");

		nodeList.stream().forEach(host -> {
			hostList.addAll(createHosts(host));
		});

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, timezone,
				costPerProc, costPerMem, costPerStorage, costPerBw);

		Datacenter datacenter = null;
		VmAllocationPolicy policy = null;
		// Create datacenter and policy for vm(These are Reflection class)
		try {
			Class<?> PolicyClass = Class.forName(policyName); // Get the policy class name from xml file
			Constructor<?> PolicyConstruct = PolicyClass.getConstructor(List.class);// get cons
			policy = (VmAllocationPolicy) PolicyConstruct.newInstance(hostList);// policy creation
			datacenter = new Datacenter(name, characteristics, policy, storageList, 0);// create datacenter
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	}

	/**
	 * createHosts is used for create host and return type is host object of list
	 * 
	 * @param host
	 * @return hostList
	 */
	public List<Host> createHosts(Node host) {

		int count = Integer.parseInt(host.selectSingleNode("count").getText());
		int pes = Integer.parseInt(host.selectSingleNode("pes").getText());
		int mips = Integer.parseInt(host.selectSingleNode("mips").getText());
		int ram = Integer.parseInt(host.selectSingleNode("ram").getText());
		int storage = Integer.parseInt(host.selectSingleNode("storage").getText());
		int bw = Integer.parseInt(host.selectSingleNode("bandwidth").getText());
		String policyName = host.selectSingleNode("policy").getText();

		List<Pe> tmpPeList = new ArrayList<Pe>(); // create PE from countpes
		for (int j = 0; j < pes; j++) {
			tmpPeList.add(new Pe(j, new PeProvisionerSimple(mips)));
		}
		List<Host> hostList = new ArrayList<Host>();
		int hostId = 0;
		for (int i = 0; i < count; i++) {
			List<Pe> peList = new ArrayList<Pe>();
			peList.addAll(tmpPeList);

			VmScheduler policy = null;
			try {
				Class<?> PolicyClass = Class.forName(policyName);// VmSchedulerSpaceShared<
				Constructor<?> PolicyConstruct = PolicyClass.getConstructor(List.class);
				policy = (VmScheduler) PolicyConstruct.newInstance(peList);

			} catch (Exception e) {
				e.printStackTrace();
			}

			hostId = i + totalHosts;
			hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
					policy));
		}
		totalHosts += hostList.size();
		return hostList;
	}

	public List<DatacenterBroker> createDatacenterBrokers() {
		List<Node> nodeList = docConfig.selectNodes("//establishment/broker[@valid='true']");
		List<DatacenterBroker> dcBrokerArray = new ArrayList<DatacenterBroker>();

		nodeList.stream().forEach(broker -> {
			dcBrokerArray.add(createDatacenterBroker(broker));// passing broker one by one
		});

		// for (Iterator<Node> iter = nodeList.iterator(); iter.hasNext();) {
		// Node node = iter.next();
		// dcBrokerArray.add(createDatacenterBroker(node));
		// }
		return dcBrokerArray;
	}

	public DatacenterBroker createDatacenterBroker(Node current) {

		String name = current.selectSingleNode("name").getText();
		Node policyNode = current.selectSingleNode("policy");
		String policyName = policyNode.getText();
		DatacenterBroker policy = null;
		Class<?> PolicyClass = null;
		try {
			PolicyClass = Class.forName(policyName);
			Constructor<?> PolicyConstruct = PolicyClass.getConstructor(String.class);// org.cloudbus.cloudsim.DatacenterBroke
			policy = (DatacenterBroker) PolicyConstruct.newInstance(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DatacenterBroker broker = policy;
		List<Vm> vmList = new ArrayList<Vm>();

		// Create the Virtual Machines
		List<Node> nodeList = current.selectNodes("vms[@valid='true']");
		// for (Iterator<Node> iter = nodeList.iterator(); iter.hasNext();) {
		// Node node = iter.next();
		// vmList.addAll(createVMs(node, broker));
		// }
		nodeList.stream().forEach(vm -> {
			vmList.addAll(createVMs(vm, broker));
		});
		// Create the cloudlets
		List<Cloudlet> cloudletList = new LinkedList<Cloudlet>();
		nodeList = current.selectNodes("cloudlets[@valid='true']");
		// idShift = 0;
		for (Iterator<Node> iter = nodeList.iterator(); iter.hasNext();) {
			Node node = iter.next();
			cloudletList.addAll(createCloudlets(node, broker));
		}

		broker.submitVmList(vmList);
		broker.submitCloudletList(cloudletList);

		/* GET SCHEDULER NAME */
		policyNode = current.selectSingleNode("bindvmcloudlet");
		if (policyNode != null) {
			policyName = policyNode.getText();
			PolicyClass = null;
			try {
				// PolicyClass = Class.forName(policyName);
				// Constructor<?> PolicyConstruct = PolicyClass.getConstructor(List.class,
				// List.class);
				// Object obj = PolicyConstruct.newInstance(vmList, cloudletList);
				// Method PolicyApproach = PolicyClass.getMethod("Execute");
				// List<Integer> results = (List<Integer>) PolicyApproach.invoke(obj);
				// for (int i = 0; i < results.size(); i += 2) {
				// int vmId = results.get(i);
				// int ctId = results.get(i + 1);
				// broker.bindCloudletToVm(ctId, vmId);
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return broker;

	}

	public List<Vm> createVMs(Node current, DatacenterBroker broker) {
		// Create the vm
		int count = Integer.parseInt(current.selectSingleNode("count").getText());
		String policyName = current.selectSingleNode("policy").getText();

		int imagesize = Integer.parseInt(current.selectSingleNode("imagesize").getText());
		int mips = Integer.parseInt(current.selectSingleNode("mips").getText());
		int ram = Integer.parseInt(current.selectSingleNode("ram").getText());
		int bw = Integer.parseInt(current.selectSingleNode("bandwidth").getText());
		int pes = Integer.parseInt(current.selectSingleNode("pes").getText());
		String vmm = current.selectSingleNode("vmm").getText();
		int userId = broker.getId();

		int vmId = 0;
		List<Vm> vmList = new ArrayList<Vm>();
		for (int i = 0; i < count; i++) {
			CloudletScheduler policy = null;
			try {
				Class<?> PolicyClass = Class.forName(policyName);
				Constructor<?> PolicyConstruct = PolicyClass.getConstructor();
				policy = (CloudletScheduler) PolicyConstruct.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			vmId = i + totalVMs;
			// mips += 50;// 2018
			Vm vm = new Vm(vmId, userId, mips, pes, ram, bw, imagesize, vmm, policy);
			vmList.add(vm);
		}
		totalVMs += vmList.size();
		return vmList;
	}

	public List<Cloudlet> createCloudlets(Node current, DatacenterBroker broker) {
		int count = Integer.parseInt(current.selectSingleNode("count").getText());
		String policyName = current.selectSingleNode("policy").getText();
		int inputsize = Integer.parseInt(current.selectSingleNode("inputsize").getText());
		int outputsize = Integer.parseInt(current.selectSingleNode("outputsize").getText());
		int length = Integer.parseInt(current.selectSingleNode("length").getText());
		int pes = Integer.parseInt(current.selectSingleNode("pes").getText());
		int userId = broker.getId();

		UtilizationModel policy = null;
		try {
			Class<?> PolicyClass = Class.forName(policyName);
			Constructor<?> PolicyConstruct = PolicyClass.getConstructor();
			policy = (UtilizationModel) PolicyConstruct.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int cloudletId = 0;
		LinkedList<Cloudlet> cloudletList = new LinkedList<Cloudlet>();
		for (int i = 0; i < count; i++) {
			int ranLen = 0;
			ranLen = length + 100 * i;
			cloudletId = totalCloudlets + i;
			Cloudlet cloudlet = new Cloudlet(cloudletId, ranLen, pes, inputsize, outputsize, policy, policy, policy);
			cloudlet.setUserId(userId);
			cloudletList.add(cloudlet);
		}
		totalCloudlets += cloudletList.size();
		return cloudletList;
	}

}
