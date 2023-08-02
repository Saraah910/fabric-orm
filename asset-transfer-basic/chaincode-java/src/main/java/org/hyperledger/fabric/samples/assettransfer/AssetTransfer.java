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
         stub.putStringState(asset.getAssetID(), assetJson1); 
 
         Owner owner = new Owner("Tomoko1", "Tomoko","Roy");      
         owner.addAssetIDs(asset.getAssetID());
         String ownerJson1 = genson.serialize(owner);  
 
         stub.putStringState(owner.getOwnerID(), ownerJson1);
          
 
 
     }
 
     /**
      *
      * @param ctx 
      * @param assetID 
      * @param color 
      * @param size 
      * @param ownerID 
      * @param appraisedValue 
      * @return 
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
         stub.putStringState(assetID, sortedJson);
 
         String ownerJSON = stub.getStringState(ownerID);  
         Owner owner = genson.deserialize(ownerJSON, Owner.class);
         owner.addAssetIDs(assetID); 
         
         String newOwnerJSON = genson.serialize(owner);
         stub.putStringState(ownerID, newOwnerJSON);
 
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
      * @param ctx 
      * @param assetID 
      * @param color 
      * @param size 
      * @param ownerID 
      * @param appraisedValue 
      * @return 
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
 
     @Transaction(intent = Transaction.TYPE.EVALUATE)
     public Owner GetOwnerOfAsset(final Context ctx, final String assetID) {
         ChaincodeStub stub = ctx.getStub();
         String assetJSON = stub.getStringState(assetID);
 
         if (assetJSON == null || assetJSON.isEmpty()) {
             String errorMessage = String.format("Asset %s does not exist", assetID);
             System.out.println(errorMessage);
             throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
         }
 
         Asset asset = genson.deserialize(assetJSON, Asset.class);
         String ownerID = asset.getOwnerID();
         Owner ownerResult = this.ReadOwner(ctx, ownerID);
         return ownerResult;
     }
 
     /**
      *
      * @param ctx 
      * @return 
      */
     @Transaction(intent = Transaction.TYPE.EVALUATE)
     public String GetAllAssetIDs(final Context ctx) {
         ChaincodeStub stub = ctx.getStub();
         List<String> queryResults = new ArrayList<String>();
 
         QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");
 
         for (KeyValue result: results) {
             Owner owner = genson.deserialize(result.getStringValue(), Owner.class);
 
             if (owner != null && owner.getName() != null) {
                 queryResults.add(owner.getOwnerID() + " : " + genson.serialize(owner.getIDsOfOwnedAssets()));
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
         List<String> assetIDtoAssetMapping = new ArrayList<String>();
 
         String ownerJson = stub.getStringState(ownerID);
 
         if (ownerJson == null || ownerJson.isEmpty()) {
             String errorMessage = String.format("Owner %s does not exist", ownerID);
             System.out.println(errorMessage);
             throw new ChaincodeException(errorMessage, AssetTransferErrors.OWNER_NOT_FOUND.toString());
         }
 
         Owner owner = genson.deserialize(ownerJson, Owner.class);
 
         for (String assetId : owner.getIDsOfOwnedAssets()) {
             Asset asset = this.ReadAsset(ctx, assetId);
             assetIDtoAssetMapping.add(assetId + " : " + asset);
         }
 
         final String response = genson.serialize(assetIDtoAssetMapping);
         return response;
     }
 
 }
 