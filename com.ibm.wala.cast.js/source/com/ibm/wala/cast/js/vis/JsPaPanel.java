package com.ibm.wala.cast.js.vis;

import java.util.ArrayList;
import java.util.List;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.cast.ipa.callgraph.AstGlobalPointerKey;
import com.ibm.wala.cast.ipa.callgraph.ObjectPropertyCatalogKey;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.OrdinalSetMapping;
import com.ibm.wala.viz.viewer.PaPanel;

/**
 * Augments the PaPanel with: 1) global pointer keys at the root level. 2) property catalog key for instance keys.
 * @author yinnonh
 *
 */
public class JsPaPanel extends PaPanel {

	private static final long serialVersionUID = 1L;

	private MutableMapping<List<ObjectPropertyCatalogKey>> instanceKeyIdToObjectPropertyCatalogKey = MutableMapping.<List<ObjectPropertyCatalogKey>> make();
	private List<AstGlobalPointerKey> globalsPointerKeys = new ArrayList<AstGlobalPointerKey>();

	public JsPaPanel(CallGraph cg, PointerAnalysis pa) {
		super(cg, pa);
		initDataStructures(pa);
	}

	private void initDataStructures(PointerAnalysis pa) {
		HeapGraph heapGraph = pa.getHeapGraph();
		OrdinalSetMapping<InstanceKey> instanceKeyMapping = pa.getInstanceKeyMapping();
		for (Object n : heapGraph){
			if (heapGraph.getPredNodeCount(n) == 0){
				if (n instanceof PointerKey){
					if (n instanceof ObjectPropertyCatalogKey){
						ObjectPropertyCatalogKey opck = (ObjectPropertyCatalogKey) n;
						InstanceKey instanceKey = opck.getObject();
						int instanceKeyId = instanceKeyMapping.getMappedIndex(instanceKey);
						mapUsingMutableMapping(instanceKeyIdToObjectPropertyCatalogKey, instanceKeyId, opck);
					} else if (n instanceof AstGlobalPointerKey){
						globalsPointerKeys.add((AstGlobalPointerKey) n);
					}
				} else {
					System.err.println("Non Pointer key root: " + n);
				}
			}
		}
	}

	protected List<PointerKey> getPointerKeysUnderInstanceKey(InstanceKey ik) {
		List<PointerKey> ret = new ArrayList<PointerKey>();
		ret.addAll(super.getPointerKeysUnderInstanceKey(ik));
		int ikIndex = pa.getInstanceKeyMapping().getMappedIndex(ik);
		ret.addAll(nonNullList(instanceKeyIdToObjectPropertyCatalogKey.getMappedObject(ikIndex)));
		return ret;
	}

	private String cgNodesRoot = "CGNodes";
	private String globalsRoot = "Globals";
	
	@Override
	protected List<Object> getRootNodes() {
		List<Object> ret = new ArrayList<Object>(2);
		ret.add(cgNodesRoot);
		ret.add(globalsRoot);
		return ret;
	}

	@Override
	protected List<Object> getChildrenFor(Object node) {
		List<Object> ret = new ArrayList<Object>();
		if (node == cgNodesRoot){
	    for (int nodeId = 0 ; nodeId < cg.getNumberOfNodes(); nodeId++){
	      CGNode cgNode = cg.getNode(nodeId);
	      ret.add(cgNode);
	    }
		} else if (node == globalsRoot){
			ret.addAll(globalsPointerKeys);
		} else {
			ret.addAll(super.getChildrenFor(node));
		}
		return ret;
	}
}
