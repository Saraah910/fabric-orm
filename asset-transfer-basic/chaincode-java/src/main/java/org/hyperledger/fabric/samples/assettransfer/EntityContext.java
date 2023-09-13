package org.hyperledger.fabric.samples.assettransfer;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

public class EntityContext extends Context{

    private EntityManager manager;
    public EntityManager getEntityManager() {
        return manager;
    }
    public EntityContext(ChaincodeStub stub, EntityManager manager) {
        super(stub);
        this.manager = manager;        
    }

}
