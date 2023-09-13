/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.assettransfer;

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
        Owner owner = new Owner("Sakshi1", "Sakshi", "Aherkar");
        manager.saveOwner(owner);

    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateAsset(final EntityContext ctx, final String assetID, final String color, final int size,
                        final String ownerID, final int appraisedValue) {
        EntityManager manager = ctx.getEntityManager();
        if (manager.AssetExists(assetID)) {
            throw new ChaincodeException("ASSET ALREADY EXISTS");
        }
        if (!manager.OwnerExists(ownerID)) {
            throw new ChaincodeException("OWNER DOES NOT EXISTS");
        }
        Asset asset = new Asset(assetID, color, size, ownerID, appraisedValue);
        manager.saveAsset(asset);

        Owner owner = manager.loadOwner(ownerID);
        owner.addAssetID(assetID);
        manager.saveOwner(owner);
        return asset;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Owner CreateOwner(final EntityContext ctx, final String ownerID, final String name, final String lastName) {
        EntityManager manager = ctx.getEntityManager();
        if (manager.OwnerExists(ownerID)) {
            throw new ChaincodeException("OWNER ALREADY EXISTS");
        }
        Owner owner = new Owner(ownerID, name, lastName);
        manager.saveOwner(owner);
        return owner;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String ReadOwner(final EntityContext ctx, final String ownerID) {
        EntityManager manager = ctx.getEntityManager();
        if (!manager.OwnerExists(ownerID)) {
            throw new ChaincodeException("OWNER DOES NOT EXISTS");
        }
        Owner owner = manager.loadOwner(ownerID);
        return genson.serialize(owner);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE) 
    public String ReadAsset(final EntityContext ctx, final String assetID) {
        EntityManager manager = ctx.getEntityManager();        
        if (!manager.AssetExists(assetID)) {
            throw new ChaincodeException("ASSET DOES NOT EXIXTS");
        }
        Asset asset = manager.loadAsset(assetID);
        return genson.serialize(asset);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String TransferAsset(final EntityContext ctx, final String assetID, final String ownerID) {
        EntityManager manager = ctx.getEntityManager();
        if (!manager.AssetExists(assetID)) {
            throw new ChaincodeException("ASSET DOES NOT EXISTS");
        }
        if (!manager.OwnerExists(ownerID)) {
            throw new ChaincodeException("OWNER DOES NOT EXISTS");
        }
        if (manager.AlreadyOwnedAsset(assetID, ownerID)) {
            throw new ChaincodeException("OWNER ALREADY OWNES ASSET %S ",assetID);
        }

        Asset asset = manager.loadAsset(assetID);
        Owner owner = manager.loadOwner(ownerID);
        asset.setOwner(owner);

        return "OWNERSHIP TRANSFRRED";
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String UpdateAsset(final EntityContext ctx, final String assetID, final String color, final int size,
                        final String ownerID, final int appraisedValue) {
        EntityManager manager = ctx.getEntityManager();
        if (!manager.OwnerExists(ownerID)) {
            throw new ChaincodeException("OWNER DOES NOT EXISTS");
        }
        if (!manager.AssetExists(assetID)) {
            throw new ChaincodeException("ASSET DOES NOT EXIST");
        }
        Asset asset = manager.loadAsset(assetID);
        if (!asset.getOwnerID().equals(ownerID)) {
            throw new ChaincodeException("OWNERSHIP CANNOT BE TRANSFRRED");
        }
        asset.setAssetID(assetID);
        asset.setColor(color);
        asset.setSize(size);
        asset.setAppraisedValue(appraisedValue);
        manager.saveAsset(asset);
        return "ASSET UPDATED";
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT) 
    public void DeleteAsset(final EntityContext ctx, final String assetID) {
        EntityManager manager = ctx.getEntityManager();
        if (!manager.AssetExists(assetID)) {
            throw new ChaincodeException("ASSET DOES NOT EXIST");
        }
        manager.deleteAsset(assetID);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset[] GetAssetsOfOwner(final EntityContext ctx, final String ownerID) {
        EntityManager manager = ctx.getEntityManager();
        if (!manager.OwnerExists(ownerID)) {
            throw new ChaincodeException("OWNER DOES NOT EXISTS");
        }
        Owner owner = manager.loadOwner(ownerID);
        String res = genson.serialize(owner.fetchAssetCollection());
        Asset[] response = genson.deserialize(res,Asset[].class);
        return response;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Owner GetOwnerOfAsset(final EntityContext ctx, final String assetID) {
        EntityManager manager = ctx.getEntityManager();
        if (!manager.AssetExists(assetID)) {
            throw new ChaincodeException("ASSET DOES NOT EXISTS");
        }
        Asset asset = manager.loadAsset(assetID);
        return asset.getOwner();
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String ViewDB(final EntityContext ctx) {
        EntityManager manager = ctx.getEntityManager();
        return manager.viewDB();
    }   
}