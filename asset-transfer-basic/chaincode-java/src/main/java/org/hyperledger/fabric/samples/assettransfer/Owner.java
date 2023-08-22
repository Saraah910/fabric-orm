/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
// import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.Context;

@DataType()
public final class Owner {

    private final Genson genson = new Genson();

    @Property()
    private final String ownerID;

    @Property()
    private final String name;

    @Property()
    private final String lastName;

    @Property()
    private ArrayList<String> OwnedAssetIDs;

    @Property()
    private String ownedAssets;

    private ArrayList<Asset> OwnedAssetList = new ArrayList<Asset>();

    public String getOwnerID() {
        return ownerID;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public ArrayList<String> getIDsOfOwnedAssets() {
        return OwnedAssetIDs;
    }

    public void addAssetIDs (final String assetID) {
        this.OwnedAssetIDs.add(assetID);
    }
    
    public void RemoveAssetID(final String assetID) {
        this.OwnedAssetIDs.remove(assetID);
    }

    public String getOwnedAssetsOfOwner(final Context ctx) {
        if (ownedAssets == null) {
            ownedAssets = fetchOwnedAssetsData(ctx);
        }
        return ownedAssets;
    }

    private String fetchOwnedAssetsData(Context ctx) {
        ChaincodeStub stub = ctx.getStub();
       
        for (String assetID: OwnedAssetIDs) {
            CompositeKey assetKey = stub.createCompositeKey(Asset.class.getSimpleName(),assetID);
            String assetJSON = stub.getStringState(assetKey.toString());

            Asset asset = genson.deserialize(assetJSON,Asset.class);
            OwnedAssetList.add(asset);
        }
        
        String OwnedAssetsResponse = genson.serialize(OwnedAssetList);
        return OwnedAssetsResponse;
    } 

    public Owner(final String ownerID, final String name, final String lastName) {
        this.ownerID = ownerID;
        this.name = name;
        this.lastName = lastName;
        this.OwnedAssetIDs = new ArrayList<String>();
        this.ownedAssets = null;
        
        
    }

    public Owner(@JsonProperty("ownerID") final String ownerID, @JsonProperty("name") final String name,
            @JsonProperty("lastName") final String lastName, @JsonProperty("iDsOfOwnedAssets") ArrayList<String> OwnedAssetIDs,
            @JsonProperty("ownedAssets") String ownedAssets) {
        this.ownerID = ownerID;
        this.name = name;
        this.lastName = lastName;
        this.OwnedAssetIDs = OwnedAssetIDs;
        this.ownedAssets = null; 
        
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Owner other = (Owner) obj;

        return Objects.deepEquals(
                new String[] {getOwnerID(), getName(), getLastName()},
                new String[] {other.getOwnerID(), other.getName(), other.getLastName()})
                &&
                Objects.deepEquals(getIDsOfOwnedAssets(), other.getIDsOfOwnedAssets())
                &&
                Objects.deepEquals(getOwnedAssetsOfOwner(null), other.getOwnedAssetsOfOwner(null));
        
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOwnerID(), getName(), getLastName(), getOwnedAssetsOfOwner(null));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [ownerID=" + ownerID + ", name="
                + name + ", lastName=" + lastName + ", iDsOfOwnedAssets=" + OwnedAssetIDs + ", ownedAssets=" + ownedAssets + "]";
    }
}
