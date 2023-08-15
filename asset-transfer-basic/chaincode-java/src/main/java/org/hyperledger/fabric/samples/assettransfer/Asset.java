/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
// import org.hyperledger.fabric.protos.peer.ChaincodeGrpc.ChaincodeStub;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Asset {
    private final Genson genson = new Genson();

    @Property()
    private final String assetID;

    @Property()
    private final String color;

    @Property()
    private final int size;

    @Property()
    private final String ownerID;

    @Property()
    private final int appraisedValue;

    @Property()
    private Owner ownerEntityLoaded;

    public String getAssetID() {
        return assetID;
    }

    public String getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void AddAssetIdToOwner(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID);
        Owner owner = genson.deserialize(stub.getStringState(ownerKey.toString()),Owner.class);
        owner.addAssetIDs(assetID);
        String ownerJSON = genson.serialize(owner);
        stub.putStringState(ownerKey.toString(), ownerJSON);
    }

    public void RemoveAssetIdFromOwner(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID);
        Owner owner = genson.deserialize(stub.getStringState(ownerKey.toString()),Owner.class);
        owner.RemoveAssetID(assetID);
        String ownerJSON = genson.serialize(owner);
        stub.putStringState(ownerKey.toString(), ownerJSON);
    }

    public int getAppraisedValue() {
        return appraisedValue;
    }

    public Owner getOwner(final Context ctx) {
        if (ownerEntityLoaded == null) {
            ownerEntityLoaded = fetchOwnerData(ctx);
        }
        return ownerEntityLoaded;
    }

    public Owner fetchOwnerData(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        CompositeKey ownerKey = stub.createCompositeKey(Owner.class.getSimpleName(),ownerID); 
        String ownerJSON = stub.getStringState(ownerKey.toString());

        Owner owner = genson.deserialize(ownerJSON,Owner.class);
        
        return owner;
    }


    public Asset(@JsonProperty("assetID") final String assetID, @JsonProperty("color") final String color,
            @JsonProperty("size") final int size, @JsonProperty("ownerID") final String ownerID,
            @JsonProperty("appraisedValue") final int appraisedValue) {
        this.assetID = assetID;
        this.color = color;
        this.size = size;
        this.ownerID = ownerID;
        this.ownerEntityLoaded = null;
        this.appraisedValue = appraisedValue;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Asset other = (Asset) obj;

        return Objects.deepEquals(
                new String[] {getAssetID(), getColor(), getOwnerID()},
                new String[] {other.getAssetID(), other.getColor(), other.getOwnerID()})
                &&
                Objects.deepEquals(
                new int[] {getSize(), getAppraisedValue()},
                new int[] {other.getSize(), other.getAppraisedValue()});
                
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssetID(), getColor(), getSize(), getOwnerID(), getAppraisedValue());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [assetID=" + assetID + ", color="
                + color + ", size=" + size + ", ownerID=" + ownerID + ", appraisedValue=" + appraisedValue + "]";
    }
}
