/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;
import java.util.Objects;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Asset {
    
    @Property()
    private String assetID;

    @Property()
    private String color;

    @Property()
    private int size;

    @Property()
    private String ownerID;

    @Property()
    private int appraisedValue;

    @Property()
    private Owner owner;

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setOwnerID(String newOwnerID) {
        this.ownerID = newOwnerID;
    }

    public void setAppraisedValue(int appraisedValue) {
        this.appraisedValue = appraisedValue;
    }
    
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

    public int getAppraisedValue() {
        return appraisedValue;
    }

    public Owner getOwner(final Context ctx) {
        if (owner == null) {
            owner = fetchOwnerData(ctx);
        }
        return owner;
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
        this.owner = null;
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