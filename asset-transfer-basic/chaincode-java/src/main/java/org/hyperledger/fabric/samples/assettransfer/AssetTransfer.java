/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "Asset Transfer",
                description = "The hyperlegendary asset transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "a.transfer@example.com",
                        name = "Adrian Transfer",
                        url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private final Genson genson = new Genson();
    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS,
        OWNER_NOT_FOUND,
        OWNER_ALREADY_EXISTS
    }

    @Override
    public Context createContext(final ChaincodeStub stub) {
        return new EntityContext(stub, new EntityManager(stub));
    }
    
    /**
     * @param ctx 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final EntityContext ctx) {
        EntityManager manager = ctx.getEntityManager();

        Asset asset = new Asset("asset1", "blue", 5, "Tomoko1", 300);
        Owner owner = new Owner("Tomoko1", "Tomoko","Roy");  
        owner.addAssetIDs(asset.getAssetID());   
        
        manager.saveAssetToLedger(asset);
        manager.saveOwnerToLedger(owner);       
    }

    /**
     @param ctx 
     @param assetID 
     @param color 
     @param size 
     @param ownerID 
     @param appraisedValue 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateNewAsset(final EntityContext ctx, final String assetID, final String color, final int size,
        final String ownerID, final int appraisedValue) {
        EntityManager manager = ctx.getEntityManager();

        if (!OwnerExists(ctx, ownerID)) {
            String errorMessage = String.format("Owner does not exists.", ownerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
        }
        if (AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s already exists", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        } 
        Asset asset = new Asset(assetID, color, size, ownerID, appraisedValue);
        Owner owner = asset.getOwner(ctx);
        owner.addAssetIDs(assetID);

        manager.saveAssetToLedger(asset); 
        manager.saveOwnerToLedger(owner);           
        return asset;
    }

    /**
     @param ctx
     @param ownerID
     @param firstName
     @param lastName
     @return
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Owner CreateOwner(final EntityContext ctx, final String ownerID, final String firstName, final String lastName) {
        EntityManager manager = ctx.getEntityManager();
        if (OwnerExists(ctx, ownerID)) {
            String errorMessage = String.format("Owner %s already exists", ownerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_ALREADY_EXISTS.toString());
        }       
        Owner owner = new Owner(ownerID, firstName, lastName);       
        manager.saveOwnerToLedger(owner);
        return owner;
    }

    /**
     * @param ctx 
     * @param assetID 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset ReadAsset(final EntityContext ctx, final String assetID) {
        EntityManager manager = ctx.getEntityManager();
        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        } 
        Asset asset = manager.loadAssetFromLedger(assetID);       
        return asset;        
    }

    /**
     @param ctx 
     @param ownerID 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Owner ReadOwner(final EntityContext ctx, final String ownerID) {
        EntityManager manager = ctx.getEntityManager();       
        if (!OwnerExists(ctx, ownerID)) {
            String errorMessage = String.format("Owner %s does not exists", ownerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
        }        
        Owner owner = manager.loadOwnerFromLedger(ownerID);        
        return owner;       
    }

    /**
     @param ctx 
     @param assetID 
     @param color 
     @param size 
     @param ownerID 
     @param appraisedValue 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset UpdateAsset(final EntityContext ctx, final String assetID, final String color, final int size, 
        final String ownerID, final int appraisedValue) {
        EntityManager manager = ctx.getEntityManager();
        if (!OwnerExists(ctx, ownerID)) {
            String errorMessage = String.format("Owner %s does not exists", ownerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
        }
        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }
        Asset asset = manager.loadAssetFromLedger(assetID);

        if (!asset.getOwnerID().equals(ownerID)) {
            String errorMessage = "Ownership cannot be updated.";
            throw new ChaincodeException(errorMessage);
        } 
        asset.setAssetID(assetID);
        asset.setColor(color);
        asset.setSize(size);
        asset.setAppraisedValue(appraisedValue);

        manager.saveAssetToLedger(asset);
        return asset;
    }

    /**
     @param ctx 
     @param assetID 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String DeleteAsset(final EntityContext ctx, final String assetID) {
        EntityManager manager = ctx.getEntityManager();
        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }        
        manager.deleteAssetFromLedger(assetID);
        String ResponeMessage = String.format("Deleted asset with ID %s ", assetID);
        return ResponeMessage;
    }    
    /**
     @param ctx 
     @param assetID 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final EntityContext ctx, final String assetID) {
        EntityManager manager = ctx.getEntityManager();
        Asset asset = manager.loadAssetFromLedger(assetID);
        try {
            return (asset.getAssetID() != null);
        } catch (Exception error) {
            return false;
        }        
    }
    /**
     @param ctx 
     @param ownerID 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean OwnerExists(final EntityContext ctx, final String ownerID) {
        EntityManager manager = ctx.getEntityManager();
        Owner owner = manager.loadOwnerFromLedger(ownerID);
        try {
            return (owner.getOwnerID() != null);
        } catch (Exception error) {
            return false;
        }       
    }
    /**
     @param ctx
     @param assetID
     @param newOwner
     @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AlreadyOwningAsset(final EntityContext ctx, final String assetID, final String newOwner) {
        EntityManager manager = ctx.getEntityManager();
        Owner owner = manager.loadOwnerFromLedger(newOwner);
        ArrayList<String> result = owner.getIDsOfOwnedAssets();

        return (result.contains(assetID));
    }

    /**
      @param ctx 
      @param assetID 
      @param newOwner 
      @return 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String TransferAsset(final EntityContext ctx, final String assetID, final String newOwnerID) {
        EntityManager manager = ctx.getEntityManager();       
        if (!OwnerExists(ctx, newOwnerID)) {
            String errorMessage = String.format("Owner %s does not exist", newOwnerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
        }
        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }
        if (AlreadyOwningAsset(ctx,assetID,newOwnerID)) {
            String errorMessage = String.format("%s Already Ownes Asset with ID %s", newOwnerID, assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        Asset asset = manager.loadAssetFromLedger(assetID);
        Owner owner = asset.getOwner(ctx);

        manager.updateAssetIDCollections(owner.getOwnerID(), newOwnerID, assetID);
        asset.setOwnerID(newOwnerID);
        manager.saveAssetToLedger(asset);

        String ResponseMessage = String.format("Ownership transfrred to %s", newOwnerID);
        return ResponseMessage;
    }

    /**
      @param ctx 
      @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset[] GetAllAssetsOfOwner(final EntityContext ctx, final String ownerID) {
        EntityManager manager = ctx.getEntityManager();  
        
        if (!OwnerExists(ctx, ownerID)) {
            throw new ChaincodeException("Owner does not exists.");
        }
        Owner owner = manager.loadOwnerFromLedger(ownerID); 
        String res = genson.serialize(owner.getOwnedAssetsOfOwner(ctx));
        Asset[] OwnedAssetArray = genson.deserialize(res,Asset[].class);
        return OwnedAssetArray;        
    }

    /**
     * @param ctx
     * @param assetID
     * @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Owner GetOwnerOfAsset(final EntityContext ctx, final String assetID) {
        EntityManager manager = ctx.getEntityManager();
        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }
        Asset asset = manager.loadAssetFromLedger(assetID);
        return asset.getOwner(ctx);        
    }

    /**
     @param ctx 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssetIDs(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        Map<String,ArrayList<String>> resultMapping = new HashMap<String,ArrayList<String>>();
        QueryResultsIterator<KeyValue> ownerEntities = stub.getStateByPartialCompositeKey(Owner.class.getSimpleName());

        for (KeyValue ownerKeyValue: ownerEntities) {
            Owner owner = genson.deserialize(ownerKeyValue.getStringValue(),Owner.class);
            resultMapping.put(owner.getOwnerID() , owner.getIDsOfOwnedAssets());
        }       
        final String response = genson.serialize(resultMapping);       
        return response; 
    }
}