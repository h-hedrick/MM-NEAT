package edu.utexas.cs.nn.evolution.genotypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import edu.utexas.cs.nn.MMNEAT.MMNEAT;
import edu.utexas.cs.nn.networks.TWEANN;
import edu.utexas.cs.nn.networks.hyperneat.HyperNEATTask;
import edu.utexas.cs.nn.networks.hyperneat.Substrate;
import edu.utexas.cs.nn.parameters.CommonConstants;
import edu.utexas.cs.nn.util.CartesianGeometricUtilities;
import edu.utexas.cs.nn.util.datastructures.Pair;
import edu.utexas.cs.nn.util.util2D.ILocated2D;
import edu.utexas.cs.nn.util.util2D.Tuple2D;

/**
 * genotype for a hyperNEAT CPPN network
 *
 * @author gillespl
 *
 */
public class HyperNEATCPPNGenotype extends TWEANNGenotype {

	private static final double BIAS = 1.0;//Necessary for most CPPN networks
	protected ArrayList<NodeGene> newNodes = null;
	protected ArrayList<LinkGene> newLinks = null;
	public int innovationID = 0;//provides unique innovation numbers for links and genes


	/**
	 * Default constructor
	 */
	public HyperNEATCPPNGenotype(){
		super();
	}

	/**
	 *Constructor for hyperNEATCPPNGenotype. Uses super constructor from TWEANNGenotype 
	 * @param links list of links between genes
	 * @param genes list of nodes in genotype
	 * @param outputNeurons number of output neurons
	 */
	public HyperNEATCPPNGenotype(ArrayList<LinkGene> links, ArrayList<NodeGene> genes, int outputNeurons) {
		super(genes, links, outputNeurons, false, false, 0);
	}

	/**
	 * Constructor for random hyperNEATCPPNGenotype. 
	 * @param networkInputs number of network inputs
	 * @param networkOutputs number of newtork outputs
	 * @param archetypeIndex index of genotype in archetype
	 */
	public HyperNEATCPPNGenotype(int networkInputs, int networkOutputs, int archetypeIndex) {
		super(networkInputs, networkOutputs, archetypeIndex); // Construct new CPPN with random weights
	}

	/**
	 * Uses another CPPN to create a TWEANN controller for the domain. This
	 * created TWEANN is unique only to the instance in which it is used. In a
	 * sense, it's a one-and-done network, which explains the lax use of
	 * innovation numbers
	 *
	 * @return TWEANN generated by CPPN
	 */
	@Override
	public TWEANN getPhenotype() {
		TWEANN cppn = super.getPhenotype();//cppn used to create TWEANN network
		HyperNEATTask hnt = (HyperNEATTask) MMNEAT.task;//creates instance of task in question
		List<Substrate> subs = hnt.getSubstrateInformation();//used to extract substrate information from domian
		List<Pair<String, String>> connections = hnt.getSubstrateConnectivity();//used to extract substrate connectity from domain
		ArrayList<NodeGene> newNodes = null;
		ArrayList<LinkGene> newLinks = null;
		innovationID = 0;//reset here just in case TWEANN reused(should not happen though)
		// Will map substrate names to index in subs List
		//needs to be switched
		HashMap<String, Integer> substrateIndexMapping = new HashMap<String, Integer>();
		for (int i = 0; i < subs.size(); i++) {
			substrateIndexMapping.put(subs.get(i).getName(), i);
		}
		// loop through connections and add links, based on contents of subs

		if(CommonConstants.speedUpHyperNEAT) {
			if(this.newLinks == null && newNodes == null) {
				newNodes = createSubstrateNodes(subs);
				newLinks = createNodeLinks(cppn, connections, subs, substrateIndexMapping);
			} else { 
				createNodeLinks(cppn, connections, subs, substrateIndexMapping);
			}
		} else {
			newNodes = createSubstrateNodes(subs);
			newLinks = createNodeLinks(cppn, connections, subs, substrateIndexMapping);
		}
		//the instantiation of the TWEANNgenotype in question 
		int phenotypeOutputs = 0;
		for(Substrate s: subs) {
			if(s.getStype() == Substrate.OUTPUT_SUBSTRATE) {
				phenotypeOutputs += s.size.t1 * s.size.t2;
			}
		}

		// Hard coded to have a single neural output module.
		// May need to fix this down the line.
		// An archetype index of -1 is used. Hopefully this won't cause problems,
		// since the archetype is only needed for mutations and crossover.
		TWEANNGenotype tg = new TWEANNGenotype(CommonConstants.speedUpHyperNEAT ? this.newNodes : newNodes, CommonConstants.speedUpHyperNEAT ? this.newLinks : newLinks, phenotypeOutputs, false, false, -1);
		return tg.getPhenotype();
	}


	/**
	 * Copies given genotype
	 * @return Copy of the CPPN genotype
	 */
	@Override
	public Genotype<TWEANN> copy() {
		int[] temp = moduleUsage; // Schrum: Not sure if keeping moduleUsage is appropriate
		ArrayList<LinkGene> linksCopy = new ArrayList<LinkGene>(this.links.size());
		for(LinkGene lg : this.links) {//needed for a deep copy
			linksCopy.add(new LinkGene(lg.sourceInnovation, lg.targetInnovation, lg.weight, lg.innovation, false));
		}

		ArrayList<NodeGene> genes = new ArrayList<NodeGene>(this.nodes.size());
		for(NodeGene ng : this.nodes) {//needed for a deep copy
			genes.add(new NodeGene(ng.ftype, ng.ntype, ng.innovation, false));
		}
		HyperNEATCPPNGenotype result = new HyperNEATCPPNGenotype(linksCopy, genes, MMNEAT.networkOutputs);

		// Schrum: Not sure if keeping moduleUsage is appropriate
		moduleUsage = temp;
		result.moduleUsage = new int[temp.length];
		System.arraycopy(this.moduleUsage, 0, result.moduleUsage, 0, moduleUsage.length);
		return result;
	}

	/**
	 * creates an array list containing all the nodes from all the substrates
	 *
	 * @param subs list of substrates extracted from domain
	 * @return array list of NodeGenes from substrates
	 */
	public ArrayList<NodeGene> createSubstrateNodes(List<Substrate> subs) {
		ArrayList<NodeGene> newNodes = new ArrayList<NodeGene>();
		for (int i = 0; i < subs.size(); i++) {//loops through substrate list
			for (int x = 0; x < subs.get(i).size.t1; x++) {//loops through x values of substrate
				for (int y = 0; y < subs.get(i).size.t2; y++) {//loops through y values of substrate
					// Substrate types and Neuron types match and use same values
					newNodes.add(new NodeGene(CommonConstants.ftype, subs.get(i).getStype(), innovationID++));
				}
			}
		}
		return newNodes;
	}

	/**
	 * creates an array list of links between substrates as dictated by
	 * connections parameter
	 *
	 * @param cppn used to evolve link weight
	 * @param connections list of different connections between substrates
	 * @param subs list of substrates in question
	 * @param sIMap hashmap that maps the substrate in question to its index in
	 * the substrate list
	 *
	 * @return array list containing all the links between substrates
	 */
	public ArrayList<LinkGene> createNodeLinks(TWEANN cppn, List<Pair<String, String>> connections, List<Substrate> subs, HashMap<String, Integer> sIMap) {
		ArrayList<LinkGene> result = new ArrayList<LinkGene>();
		for (int i = 0; i < connections.size(); i++) {
			int sourceSubstrateIndex = sIMap.get(connections.get(i).t1);
			int targetSubstrateIndex = sIMap.get(connections.get(i).t2);
			Substrate sourceSubstrate = subs.get(sourceSubstrateIndex);
			Substrate targetSubstrate = subs.get(targetSubstrateIndex);
			result.addAll(loopThroughLinks(cppn, i, sourceSubstrate, targetSubstrate, sourceSubstrateIndex, targetSubstrateIndex, subs));//adds links from between two substrates to whole list of links
		}
		return result;
	}

	/**
	 * a method for looping through all nodes of two substrates to be linked
	 * Link is only created if cppn output reaches a certain threshold that is
	 * dictated via command line parameter.
	 *
	 * @param cppn used to evolve link weight
	 * @param outputIndex index from cppn outputs to be used as weight in
	 * creating link
	 * @param s1 first substrate to be linked
	 * @param s2 second substrate to be linked
	 * @param s1Index index of first substrate in substrate list
	 * @param s2Index index of second substrate in substrate list
	 * @param subs list of substrates
	 *
	 * @return array list containing the genes linked between the two substrates
	 */
	public ArrayList<LinkGene> loopThroughLinks(TWEANN cppn, int outputIndex, Substrate s1, Substrate s2, int s1Index, int s2Index, List<Substrate> subs) {
		boolean speedUp = CommonConstants.speedUpHyperNEAT;
		ArrayList<LinkGene> links = null;
		for (int X1 = 0; X1 < s1.size.t1; X1++) {//searches through width of first substrate
			for (int Y1 = 0; Y1 < s1.size.t2; Y1++) {//searches through height of first substrate
				for (int X2 = 0; X2 < s2.size.t1; X2++) {//searches through width of second substrate
					for (int Y2 = 0; Y2 < s2.size.t2; Y2++) {//searches through height of second substrate
						// CPPN inputs need to be centered and scaled
						ILocated2D scaledSourceCoordinates = CartesianGeometricUtilities.centerAndScale(new Tuple2D(X1, Y1), s1.size.t1, s1.size.t2);
						ILocated2D scaledTargetCoordinates = CartesianGeometricUtilities.centerAndScale(new Tuple2D(X2, Y2), s2.size.t1, s2.size.t2);
						double[] inputs = {
								scaledSourceCoordinates.getX(),
								scaledSourceCoordinates.getY(),
								scaledTargetCoordinates.getX(),
								scaledTargetCoordinates.getY(),
								BIAS}; // inputs to CPPN
								double[] outputs = cppn.process(inputs);
								if(!speedUp) {
									links = new ArrayList<LinkGene>();
									if (Math.abs(outputs[outputIndex]) > CommonConstants.linkExpressionThreshold) {
										links.add(new LinkGene(
												getInnovationID(X1, Y1, s1Index, subs),
												getInnovationID(X2, Y2, s2Index, subs),
												calculateWeight(outputs[outputIndex]),
												innovationID++,
												false));
									} else { //added this feature so all link genes instantiated
										//weight is 0 if out of range though, which acts like there
										//is no link there.
										links.add(new LinkGene(
												getInnovationID(X1, Y1, s1Index, subs),
												getInnovationID(X2, Y2, s2Index, subs),
												0.0, innovationID++,false));
									}
								} else {
									if(Math.abs(outputs[outputIndex]) > CommonConstants.linkExpressionThreshold) {
										this.newLinks.get(X1*Y1 + X2*Y2).weight = calculateWeight(outputs[outputIndex]);
									} else { this.newLinks.get(X1*Y1 + X2*Y2).weight = 0.0;}
								}

					}
				}
			}
		}
		return links;
	}

	/**
	 * returns the innovation id of the node in question
	 *
	 * @param x x-coordinate of node
	 * @param y y-coordinate of node
	 * @param sIndex index of substrate in question
	 * @param subs list of substrates available
	 *
	 * @return innovationID of link in question
	 */
	public long getInnovationID(int x, int y, int sIndex, List<Substrate> subs) {
		long innovationIDAccumulator = 0;
		for (int i = 0; i < sIndex; i++) {
			Substrate s = subs.get(i);
			innovationIDAccumulator += s.size.t1 * s.size.t2;
		}
		innovationIDAccumulator += (subs.get(sIndex).size.t1 * y) + x;
		return innovationIDAccumulator;
	}

	/**
	 * Used for standard HyperNEAT link expression. If a link is to be
	 * expressed, then values beyond a threshold slide back to 0 so that weights
	 * with a small magnitude are possible.
	 *
	 * @param originalOutput original CPPN output
	 * @return Scaled synaptic weight
	 */
	protected double calculateWeight(double originalOutput) {
		assert (Math.abs(originalOutput) > CommonConstants.linkExpressionThreshold) : "This link should not be expressed: " + originalOutput;
		if (originalOutput > CommonConstants.linkExpressionThreshold) {
			return originalOutput - CommonConstants.linkExpressionThreshold;
		} else {
			return originalOutput + CommonConstants.linkExpressionThreshold;
		}
	}

	/**
	 * Creates a new random instance of the hyperNEATCPPNGenotype
	 */
	@Override
	public Genotype<TWEANN> newInstance() {
		HyperNEATCPPNGenotype result = new HyperNEATCPPNGenotype(MMNEAT.networkInputs, MMNEAT.networkOutputs, this.archetypeIndex);
		result.moduleUsage = new int[result.numModules];
		return result;
	}


}
