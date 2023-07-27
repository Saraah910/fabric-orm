/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

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

    /**
     * Creates some initial assets on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        

        Asset asset = new Asset("asset1", "blue", 5, "Tomoko1", 300);
        String assetJson1 = genson.serialize(asset);

        Owner owner = new Owner("Tomoko1", "Tomoko","Roy");
        owner.setAsset(asset.getAssetID());
        String ownerJson1 = genson.serialize(owner);  

        stub.putStringState(owner.getOwnerID(), ownerJson1);
        stub.putStringState(asset.getAssetID(), assetJson1);  


    }

    /**
     *
     * @param ctx the transaction context
     * @param assetID the ID of the new asset
     * @param color the color of the new asset
     * @param size the size for the new asset
     * @param ownerID the owner of the new asset
     * @param appraisedValue the appraisedValue of the new asset
     * @return the created asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateNewAsset(final Context ctx, final String assetID, final String color, final int size,
        final String ownerID, final int appraisedValue) {
        ChaincodeStub stub = ctx.getStub();

        if (!OwnerExists(ctx, ownerID)) {
            throw new ChaincodeException("Owner does not exists.");
        }

        if (AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s already exists", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        } 

        Asset asset = new Asset(assetID, color, size, ownerID, appraisedValue);
        String sortedJson = genson.serialize(asset);
        
        String ownerJson = stub.getStringState(ownerID);
        Owner owner = genson.deserialize(ownerJson,Owner.class);
        owner.setAsset(assetID);
        String newOwnerJson = genson.serialize(owner);
        stub.putStringState(ownerID, newOwnerJson);
        
        //{ownerId : ["assetId1", "assetId2"]} like this.
        stub.putStringState(assetID, sortedJson);

        return asset;
    }



    /**
     * 
     * @param ctx
     * @param ownerID
     * @param firstName
     * @param lastName
     * @return
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
        
        String sortedJson = genson.serialize(owner);
        stub.putStringState(ownerID, sortedJson);

        return owner;
    }

    /**
     *
     * @param ctx 
     * @param assetID 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset ReadAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);
        return asset;
    }

    /**
     * * Retrieves an owner with the specified ID from the ledger.
     *
     * @param ctx 
     * @param ownerID 
     * @return Owner data in Json form
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Owner ReadOwner(final Context ctx, final String ownerID) {
        ChaincodeStub stub = ctx.getStub();
        String ownerJSON = stub.getStringState(ownerID);

        if (ownerJSON == null || ownerJSON.isEmpty()) {
            String errorMessage = String.format("Owner %s does not exist", ownerID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
        }

        Owner owner = genson.deserialize(ownerJSON, Owner.class);
        return owner;
    }

    /**
     *
     * @param ctx the transaction context
     * @param assetID the ID of the asset being updated
     * @param color the color of the asset being updated
     * @param size the size of the asset being updated
     * @param ownerID the owner of the asset being updated
     * @param appraisedValue the appraisedValue of the asset being updated
     * @return the transferred asset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset UpdateAsset(final Context ctx, final String assetID, final String color, final int size,
        final String ownerID, final int appraisedValue) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset newAsset = new Asset(assetID, color, size, ownerID, appraisedValue);
        
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetID, sortedJson);
        return newAsset;
    }

    /**
     *
     * @param ctx 
     * @param assetID 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        stub.delState(assetID);
    }

    /**
     *
     * @param ctx 
     * @param assetID 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     *
     * @param ctx 
     * @param ownerID 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean OwnerExists(final Context ctx, final String ownerID) {
        ChaincodeStub stub = ctx.getStub();
        String ownerJSON = stub.getStringState(ownerID);

        return (ownerJSON != null && !ownerJSON.isEmpty());
    }

    /**
     *
     * @param ctx 
     * @param assetID 
     * @param newOwner 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String TransferAsset(final Context ctx, final String assetID, final String newOwner) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);

        Asset newAsset = new Asset(asset.getAssetID(), asset.getColor(), asset.getSize(), newOwner, asset.getAppraisedValue());
        String sortedJson = genson.serialize(newAsset);
        stub.putStringState(assetID, sortedJson);

        return asset.getOwnerID();
    }

    /**
     *
     * @param ctx 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        List<String> queryResults = new ArrayList<String>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Owner owner = genson.deserialize(result.getStringValue(), Owner.class);
            String ownerAssets = genson.serialize(owner.getOwnedAssets());
            // System.out.println(asset);
            // if (asset.getAssetID() != null) {
            //     queryResults.add("{ " + asset.getOwnerID() + ":" + asset.getAssetID() + " }");
            // }   
            if (owner.getName() != null) {
                queryResults.add("{ " + owner.getOwnerID() + " : " + ownerAssets + " }");
            }
            
                    
        }

        final String response = genson.serialize(queryResults);

        return response;
 
    }

    /**
     *
     * @param ctx 
     * @return 
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssetsOfOwner(final Context ctx, final String ownerID) {
        ChaincodeStub stub = ctx.getStub();

        String ownerJson = stub.getStringState(ownerID);
        Owner owner = genson.deserialize(ownerJson, Owner.class);

        final String response = genson.serialize(owner.getOwnedAssets());
        return response;
    }

}
