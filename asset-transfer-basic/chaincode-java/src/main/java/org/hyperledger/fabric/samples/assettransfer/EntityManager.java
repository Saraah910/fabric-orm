package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

public class EntityManager {
    Genson genson = new Genson();
    ChaincodeStub stub;

    public void saveAsset(Asset asset) {
        asset.setEntityManager(this);
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), asset.getAssetID());
        String assetJSON = genson.serialize(asset);
        stub.putStringState(assetKey.toString(), assetJSON);
    }

    public void saveOwner(Owner owner) {
        owner.setEntityManager(this);
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), owner.getOwnerID());
        String ownerJSON = genson.serialize(owner);
        stub.putStringState(ownerKey.toString(), ownerJSON);
    }

    public Asset loadAsset(String assetID) {
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        Asset asset = genson.deserialize(assetJSON,Asset.class);
        asset.setEntityManager(this);
        return asset;
    }

    public Owner loadOwner(String ownerID) {
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());
        Owner owner = genson.deserialize(ownerJSON,Owner.class);
        owner.setEntityManager(this);
        return owner;
    }

    public void deleteAsset(String assetID) {
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        Asset asset = loadAsset(assetID);
        Owner owner = loadOwner(asset.getOwnerID());
        owner.removeAssetID(assetID);
        saveOwner(owner);
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
            if (asset.getAssetID() != null) {
                System.out.print("Asset exists");
            }
            return true;
        } catch(Exception error) {
            return false;
        }
    }

    public boolean OwnerExists(String ownerID) {
        try{
            Owner owner = loadOwner(ownerID);
            if (owner.getOwnerID() != null) {
                System.out.print("Owner exists");
            }
            return true;
        } catch(Exception error) {
            return false;
        }
    }

    public boolean AlreadyOwnedAsset(final String assetID, final String newOwnerID) {
        Owner owner = loadOwner(newOwnerID);
        ArrayList<String> ownedAssetIDs = owner.getMyAssetIDCollection();

        return (ownedAssetIDs.contains(assetID));
    }
    public EntityManager(ChaincodeStub stub) {
        this.stub = stub;
    }
}