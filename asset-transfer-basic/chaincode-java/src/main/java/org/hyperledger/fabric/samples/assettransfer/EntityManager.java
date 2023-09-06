/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.assettransfer;
import java.util.ArrayList;
import java.util.HashMap;

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
            Asset asset = assetCache.get(assetID);
            System.out.println("Asset loaded from cache");
            asset.setEntityManager(this);
            return asset;
        } 
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        Asset asset = genson.deserialize(assetJSON, Asset.class);
        System.out.println("Asset loaded from ledger");
        asset.setEntityManager(this);
        return asset;    
        
    }

    public Owner loadOwnerFromLedger(String ownerID) {  
        if (ownerCache.containsKey(ownerID)) {            
            Owner owner = ownerCache.get(ownerID);
            System.out.println("Owner loaded from cache");
            owner.setEntityManager(this);
            return owner;
        }
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());
        Owner owner = genson.deserialize(ownerJSON, Owner.class);
        owner.setEntityManager(this);
        System.out.println("Owner loaded from ledger");
        return owner;
        
        
    }

    public void saveAssetToLedger(Asset asset) {
        asset.setEntityManager(this);
        String assetJSON = genson.serialize(asset);
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), asset.getAssetID());
        stub.putStringState(assetKey.toString(), assetJSON);    
        assetCache.put(asset.getAssetID(), asset);          
    }

    public void saveOwnerToLedger(Owner owner) {
        owner.setEntityManager(this);
        String ownerJSON = genson.serialize(owner);
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), owner.getOwnerID());
        stub.putStringState(ownerKey.toString(), ownerJSON);
        ownerCache.put(owner.getOwnerID(), owner);       
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
        try {
            Asset asset = this.loadAssetFromLedger(assetID);
            System.out.println("Asset loaded");
            if (asset.getAssetID() != null) {
                System.out.println("Asset exists");
            }
            return true;
        } catch (Exception error) {
            System.out.println("Asset not exists");
            return false;
        }        
    }

    public boolean OwnerExists(final String ownerID) {
        try {
            Owner owner = this.loadOwnerFromLedger(ownerID);
            System.out.println("Owner loaded");
            if (owner.getOwnerID() != null) {

            }
            System.out.println("Owner exists");
            return true;
        } catch (Exception error) {
            System.out.println("Owner not exists");
            return false;
        }       
    }
    
    public boolean AlreadyOwningAsset(final String assetID, final String newOwner) {
        Owner owner = this.loadOwnerFromLedger(newOwner);
        owner.setEntityManager(this);
        ArrayList<String> result = owner.getIDsOfOwnedAssets();

        return (result.contains(assetID));
    }

    public EntityManager(ChaincodeStub stub) {
        this.stub = stub; 

    }

}
