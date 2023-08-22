/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.routing.TransactionType;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONObject;
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

    /**
     *
     * @param ctx 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        Asset asset = new Asset("asset1", "blue", 5, "Tomoko1", 300);
        String assetJson1 = genson.serialize(asset);

        Owner owner = new Owner("Tomoko1", "Tomoko","Roy");  
        owner.addAssetIDs(asset.getAssetID());  
        String ownerJson1 = genson.serialize(owner); 

        CompositeKey assetCompositeKey = stub.createCompositeKey(Asset.class.getSimpleName(),asset.getAssetID());
        stub.putStringState(assetCompositeKey.toString(), assetJson1);          
        CompositeKey ownerCompositeKey = stub.createCompositeKey(Owner.class.getSimpleName(),owner.getOwnerID());
        stub.putStringState(ownerCompositeKey.toString(), ownerJson1);
        

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
    public Asset CreateNewAsset(final Context ctx, final String assetID, final String color, final int size,
        final String ownerID, final int appraisedValue) {
        ChaincodeStub stub = ctx.getStub();

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
        asset.AddAssetIdToOwner(ctx);
        String sortedJson = genson.serialize(asset);
        CompositeKey assetCompositeKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
    
        stub.putStringState(assetCompositeKey.toString(), sortedJson);       
        
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
    public Owner CreateOwner(final Context ctx, final String ownerID, final String firstName, final String lastName) {
        ChaincodeStub stub = ctx.getStub();

        if (OwnerExists(ctx, ownerID)) {
            String errorMessage = String.format("Owner %s already exists", ownerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_ALREADY_EXISTS.toString());
        }       
        Owner owner = new Owner(ownerID, firstName, lastName);
        CompositeKey ownerCompositekey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID);
        String sortedJson = genson.serialize(owner);
        stub.putStringState(ownerCompositekey.toString(), sortedJson);

        return owner;
    }

    /**
     * @param ctx 
     * @param assetID 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset ReadAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        
        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        } 

        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        Asset asset = genson.deserialize(assetJSON ,Asset.class);
        
        return asset;
        
    }

    /**
     @param ctx 
     @param ownerID 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Owner ReadOwner(final Context ctx, final String ownerID) {
        ChaincodeStub stub = ctx.getStub();
        
        if (!OwnerExists(ctx, ownerID)) {
            String errorMessage = String.format("Owner %s does not exists", ownerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
        }
        
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());
        Owner owner = genson.deserialize(ownerJSON,Owner.class);
        
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
    public Asset UpdateAsset(final Context ctx, final String assetID, final String color, final int size, 
        final String ownerID, final int appraisedValue) {
        ChaincodeStub stub = ctx.getStub();

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

        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
        String assetJSON = stub.getStringState(assetKey.toString());

        Asset asset = genson.deserialize(assetJSON,Asset.class);
        if (!asset.getOwnerID().equals(ownerID)) {
            String errorMessage = "Ownership cannot be updated.";
            throw new ChaincodeException(errorMessage);
        } 

        Asset newAsset = new Asset(assetID, color, size, ownerID, appraisedValue);
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetKey.toString(), sortedJson);

        return newAsset;
    }

    /**
     @param ctx 
     @param assetID 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String DeleteAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        CompositeKey CompositeAssetKeyToBeDeleted = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
        String assetJSON = stub.getStringState(CompositeAssetKeyToBeDeleted.toString());

        Asset asset = genson.deserialize(assetJSON,Asset.class);
        asset.RemoveAssetIdFromOwner(ctx);

        stub.delState(CompositeAssetKeyToBeDeleted.toString());
        String ResponeMessage = String.format("Deleted asset with ID %s ", assetID);
        return  ResponeMessage;
    }

    /**
     @param ctx 
     @param assetID 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        
        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     @param ctx 
     @param ownerID 
     @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean OwnerExists(final Context ctx, final String ownerID) {
        ChaincodeStub stub = ctx.getStub();
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(), ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());

        return (ownerJSON != null && !ownerJSON.isEmpty());
    }

    /**
     @param ctx
     @param assetID
     @param newOwner
     @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AlreadyOwningAsset(final Context ctx, final String assetID, final String newOwner) {
        ChaincodeStub stub = ctx.getStub();

        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),newOwner);
        Owner owner = genson.deserialize(stub.getStringState(ownerKey.toString()), Owner.class);
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
    public String TransferAsset(final Context ctx, final String assetID, final String newOwner) {

        ChaincodeStub stub = ctx.getStub();
        if (!OwnerExists(ctx, newOwner)) {
            String errorMessage = String.format("Owner %s does not exist", newOwner);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
        }

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        if (AlreadyOwningAsset(ctx,assetID,newOwner)) {
            String errorMessage = String.format("%s Already Ownes Asset with ID %s", newOwner, assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(), assetID);
        String assetJSON = stub.getStringState(assetKey.toString());
        Asset asset = genson.deserialize(assetJSON, Asset.class);
        asset.RemoveAssetIdFromOwner(ctx);

        Asset newAsset = new Asset(asset.getAssetID(), asset.getColor(), asset.getSize(), newOwner, asset.getAppraisedValue());
        newAsset.AddAssetIdToOwner(ctx);

        String newAssetJSON = genson.serialize(newAsset);
        stub.putStringState(assetKey.toString(), newAssetJSON);
        String ResponseMessage = String.format("Ownership transfrred to %s", newOwner);
        return ResponseMessage;
    }


    /**
      @param ctx 
      @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset[] GetAllAssetsOfOwner(final Context ctx, final String ownerID) {
        ChaincodeStub stub = ctx.getStub();
        
        if (!OwnerExists(ctx, ownerID)) {
            throw new ChaincodeException("Owner does not exists.");
        }

        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID);
        String ownerJSON = stub.getStringState(ownerKey.toString());
        Owner owner = genson.deserialize(ownerJSON, Owner.class); 

        Asset[] res = genson.deserialize(owner.getOwnedAssetsOfOwner(ctx), Asset[].class);
        
        return res;
        
    }

    /**
     * @param ctx
     * @param assetID
     * @return
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Owner GetOwnerOfAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }
        CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
        String assetJSON = stub.getStringState(assetKey.toString());

        Asset asset = genson.deserialize(assetJSON, Asset.class);
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
