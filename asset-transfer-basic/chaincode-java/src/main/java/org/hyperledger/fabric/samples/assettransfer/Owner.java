/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
// import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
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
    private final List<String> ownedAssetIDs;

    @Property()
    private boolean isDataLoaded;
   

    public String getOwnerID() {
        return ownerID;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public void AddAssetIDs (final String assetID) {
        this.ownedAssetIDs.add(assetID);
    }
    
    public void RemoveAssetID(final String assetID) {
        this.ownedAssetIDs.remove(assetID);
    }

    // public ArrayList<String> getIDsOfOwnedAssets() {
    //     return ownedAssetIDs;
    // }

    public List<String> getOwnedAssetsOfOwner(final Context ctx) {
        if (!this.isDataLoaded) {
            fetchOwnedAssetsData(ctx);
            isDataLoaded = true;
        }
        return ownedAssetIDs;
    }

    private List<String> fetchOwnedAssetsData(Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        // ArrayList<Asset> ownedAssets = new ArrayList<>();
        
        QueryResultsIterator<KeyValue> assetKeyValueIterator = stub.getStateByPartialCompositeKey(Asset.class.getSimpleName());
        for (KeyValue assetKeyValue: assetKeyValueIterator) {
            Asset asset = genson.deserialize(assetKeyValue.getStringValue(),Asset.class);
            if (asset.getOwnerID().equals(ownerID)) {
                ownedAssetIDs.add(asset.getAssetID());
            } else{
                continue;
            }
            
        }
        
        return ownedAssetIDs;
    } 

    public Owner(final String ownerID, final String name, final String lastName) {
        this.ownerID = ownerID;
        this.name = name;
        this.lastName = lastName;
        this.ownedAssetIDs = new ArrayList<String>();
        this.isDataLoaded = false;
        
    }

    public Owner(@JsonProperty("ownerID") final String ownerID, @JsonProperty("name") final String name,
            @JsonProperty("lastName") final String lastName, @JsonProperty("iDsOfOwnedAssets") final ArrayList<String> ownedAssetIDs) {
        this.ownerID = ownerID;
        this.name = name;
        this.lastName = lastName;
        this.ownedAssetIDs = ownedAssetIDs;
        this.isDataLoaded = true;
        
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
                // &&
                // Objects.deepEquals(getIDsOfOwnedAssets(), other.getIDsOfOwnedAssets())
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
                + name + ", lastName=" + lastName + ", iDsOfOwnedAssets=" + ownedAssetIDs + "]";
    }
}
