/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.assettransfer;
import java.util.ArrayList;
import java.util.HashMap;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import com.owlike.genson.Genson;

public class EntityManager {

    private final Genson genson = new Genson();
    private ChaincodeStub stub;

    private HashMap<String, Asset> assetCache = new HashMap<String,Asset>();
    private HashMap<String, Owner> ownerCache = new HashMap<String,Owner>();

    public Asset loadAssetFromLedger(String assetID) {    
        if (assetCache.containsKey(assetID)) {
            return assetCache.get(assetID);
        }        
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        Asset asset = genson.deserialize(assetJSON, Asset.class);
        // asset.setEntityManager(this);
        return asset;
    }

    public Owner loadOwnerFromLedger(String ownerID) {  
        if (ownerCache.containsKey(ownerID)) {
            return ownerCache.get(ownerID);
        }   
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());
        Owner owner = genson.deserialize(ownerJSON, Owner.class);
        // owner.setEntityManager(this);
        return owner;
    }

    public void saveAssetToLedger(Asset asset) {
        String assetJSON = genson.serialize(asset);
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), asset.getAssetID());
        stub.putStringState(assetKey.toString(), assetJSON);    
        assetCache.put(asset.getAssetID(), asset);   
        // asset.setEntityManager(this);
    }

    public void saveOwnerToLedger(Owner owner) {
        String ownerJSON = genson.serialize(owner);
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), owner.getOwnerID());
        stub.putStringState(ownerKey.toString(), ownerJSON);
        ownerCache.put(owner.getOwnerID(), owner);
        // owner.setEntityManager(this);
    }

    public void deleteAssetFromLedger(String assetID) {
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        Asset asset = this.loadAssetFromLedger(assetID);
        Owner owner = this.loadOwnerFromLedger(asset.getOwnerID());
        owner.RemoveAssetID(assetID);
        this.saveOwnerToLedger(owner);
        stub.delState(assetKey.toString());
    }

    public boolean AssetExists(String assetID) {
        Asset asset = this.loadAssetFromLedger(assetID);
        try {
            return (asset.getAssetID() != null);
        } catch (Exception error) {
            return false;
        }        
    }

    public boolean OwnerExists(final String ownerID) {
        Owner owner = this.loadOwnerFromLedger(ownerID);
        try {
            return (owner.getOwnerID() != null);
        } catch (Exception error) {
            return false;
        }       
    }
    
    public boolean AlreadyOwningAsset(final String assetID, final String newOwner) {
        Owner owner = this.loadOwnerFromLedger(newOwner);
        ArrayList<String> result = owner.getIDsOfOwnedAssets();

        return (result.contains(assetID));
    }

    public EntityManager(ChaincodeStub stub) {
        this.stub = stub; 
    }

}
