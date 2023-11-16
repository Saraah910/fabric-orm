/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@DataType
public final class EntityManager {
    Genson genson = new Genson();
    ChaincodeStub stub;

    private Map<String, Asset> assetCache;
    private Map<String, Owner> ownerCache;
    private static Set<Asset> updatedAssets = new HashSet<>();

    void save(Object obj) {
        if (obj == null) {
            throw new ChaincodeException("Invalid object or object class");
        }
        CompositeKey objectKey = stub.createCompositeKey(obj.getClass().getSimpleName(), getObjectID(obj));
        String objJSON = genson.serialize(obj);
        if (obj.getClass() == Asset.class) {
            this.assetCache.put(getObjectID(obj), (Asset) obj);
            String ownerID = ((Asset) obj).getOwnerID();
            Owner owner = loadOwner(ownerID);
            ArrayList<String> ownedAssetIDList = owner.getMyAssetIDCollection();
            if (!ownedAssetIDList.contains(getObjectID(obj))) {
                owner.addAsset((Asset) obj);
            }
            save(owner);
        } else if (obj.getClass() == Owner.class) {
            this.ownerCache.put(getObjectID(obj), (Owner) obj);
        }
        stub.putStringState(objectKey.toString(), objJSON);
        
    }
    private String getObjectID(Object obj) {
        if (obj.getClass() == Asset.class) {
            ((Asset) obj).setEntityManager(this);
            return ((Asset) obj).getAssetID();
        } else if (obj.getClass() == Owner.class) {
            ((Owner) obj).setEntityManager(this);
            return ((Owner) obj).getOwnerID();
        }
        throw new ChaincodeException("Unsupported object type");
    }

    public Asset loadAsset(String assetID) {
        if (this.assetCache.containsKey(assetID)) {
            Asset asset = this.assetCache.get(assetID);
            asset.setEntityManager(this);
            return asset;
        }
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        if (assetJSON.isEmpty()) {
            throw new ChaincodeException("ASSET DOES NOT EXIST", assetID);
        }
        Asset asset = genson.deserialize(assetJSON,Asset.class);
        asset.setEntityManager(this);
        asset.addPropertyChangeListner(asset.getOwner():: handleAssetUpdate);
        return asset;
    }

    public Owner loadOwner(String ownerID) {
        if (this.ownerCache.containsKey(ownerID)) {
            Owner owner = this.ownerCache.get(ownerID);
            owner.setEntityManager(this);
            return owner;
        }
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());
        
        if (ownerJSON.isEmpty()) {
            throw new ChaincodeException("OWNER DOES NOT EXISTS",ownerID);
        }
        Owner owner = genson.deserialize(ownerJSON,Owner.class);
        owner.setEntityManager(this);
        return owner;
    }

    public void addUpdatedAsset(Asset asset) {
        updatedAssets.add(asset);
    }

    public Set<Asset> getUpdatedAssets() {
        return updatedAssets;
    }
    // public void UpdateAsset(Asset asset) {       
    //     save(asset);
    //     EntityManager.updatedAssets.add(asset);
    // }

    // public void Finalize() {
    //     for(Asset asset : EntityManager.updatedAssets) {
    //         save(asset);
    //     }
    //     updatedAssets.clear();
    // }

    public void deleteAsset(String assetID) {
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        Asset asset = loadAsset(assetID);
        Owner owner = loadOwner(asset.getOwnerID());
        owner.removeAsset(asset);
        save(owner);
        stub.delState(assetKey.toString());
    }

    public String viewDB() {
        Map<String,ArrayList<String>> resultMapping = new HashMap<String,ArrayList<String>>();
        QueryResultsIterator<KeyValue> results = stub.getStateByPartialCompositeKey(Owner.class.getSimpleName());
        for (KeyValue result: results) {
            Owner owner = genson.deserialize(result.getStringValue(),Owner.class);
            resultMapping.put(owner.getOwnerID(), owner.getMyAssetIDCollection());
        }
        String response = genson.serialize(resultMapping);
        return response;
    }
    public boolean AssetExists(String assetID) {
        try{
            Asset asset = loadAsset(assetID);
            return (asset.getAssetID() != null);
        } catch(Exception error) {
            return false;
        }
    }

    public boolean OwnerExists(String ownerID) {
        try{
            Owner owner = loadOwner(ownerID);
            return (owner.getOwnerID() != null);
        } catch(Exception error) {
            return false;
        }
    }

    public boolean AlreadyOwnedAsset(final String assetID, final String newOwnerID) {
        Owner owner = loadOwner(newOwnerID);
        ArrayList<String> ownedAssetIDs = owner.getMyAssetIDCollection();
        if (ownedAssetIDs.contains(assetID)) {
            throw new ChaincodeException("OWNER ALREADY OWNS ASSET", assetID);
        }
        return ownedAssetIDs.contains(assetID);    
    }

    public EntityManager(ChaincodeStub stub) {
        this.stub = stub;
        this.assetCache = new HashMap<>();
        this.ownerCache = new HashMap<>();
    }
}