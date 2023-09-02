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
public final class Asset{

    // transient private EntityContext context;

    // public void setEntityContext(EntityContext ctx) {
    //     this.context = ctx;
    // }

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

    public void setAssetID(String assetID) {
        this.assetID = assetID;
    }

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

    public void setOwner(EntityContext ctx, Owner newOwner) {
        EntityManager entityManager = ctx.getEntityManager();
        if (ownerID != null) {
            Owner oldOwner = entityManager.loadOwnerFromLedger(ownerID);
            oldOwner.RemoveAssetID(assetID);
            entityManager.saveOwnerToLedger(oldOwner);
            this.setOwnerID(newOwner.getOwnerID());
            entityManager.saveAssetToLedger(this);
            newOwner.addAssetIDs(assetID);
            entityManager.saveOwnerToLedger(newOwner);
        }
    }
    public Owner getOwner(EntityContext ctx) { 
        EntityManager entityManager = ctx.getEntityManager();     
        if (owner == null) {
            owner = entityManager.loadOwnerFromLedger(ownerID);
        }
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