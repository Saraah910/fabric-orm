/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.assettransfer;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import com.owlike.genson.Genson;

public class EntityManager {

    private final Genson genson = new Genson();
    private ChaincodeStub stub;

    public Asset loadAssetFromLedger(String assetID) {            
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        Asset asset = genson.deserialize(assetJSON, Asset.class);
        return asset;
    }

    public Owner loadOwnerFromLedger(String ownerID) {     
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());
        Owner owner = genson.deserialize(ownerJSON, Owner.class);
        return owner;
    }

    public void saveAssetToLedger(Asset asset) {
        String assetJSON = genson.serialize(asset);
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), asset.getAssetID());
        stub.putStringState(assetKey.toString(), assetJSON);       
    }

    public void saveOwnerToLedger(Owner owner) {
        String ownerJSON = genson.serialize(owner);
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), owner.getOwnerID());
        stub.putStringState(ownerKey.toString(), ownerJSON);
    }

    public void deleteAssetFromLedger(String assetID) {
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        Asset asset = this.loadAssetFromLedger(assetID);
        Owner owner = this.loadOwnerFromLedger(asset.getOwnerID());
        owner.RemoveAssetID(assetID);

        this.saveOwnerToLedger(owner);
        stub.delState(assetKey.toString());
    }
    
    public void updateAssetIDCollections(String ownerID, String newOwnerID, String assetID) {

        CompositeKey ownerkey = stub.createCompositeKey(Owner.class.getSimpleName(), ownerID);
        CompositeKey newOwnerkey = stub.createCompositeKey(Owner.class.getSimpleName(), newOwnerID);

        String ownerJSONString = stub.getStringState(ownerkey.toString());
        Owner owner = genson.deserialize(ownerJSONString,Owner.class);
        owner.RemoveAssetID(assetID);
        String ownerJSON = genson.serialize(owner);

        String newOwnerJSONString = stub.getStringState(newOwnerkey.toString());
        Owner newOwner = genson.deserialize(newOwnerJSONString, Owner.class);
        newOwner.addAssetIDs(assetID);
        String newOwnerJSON = genson.serialize(newOwner);

        stub.putStringState(ownerkey.toString(), ownerJSON);
        stub.putStringState(newOwnerkey.toString(), newOwnerJSON);
    }

    public EntityManager(Context ctx) {
        this.stub = ctx.getStub();
    }

}
