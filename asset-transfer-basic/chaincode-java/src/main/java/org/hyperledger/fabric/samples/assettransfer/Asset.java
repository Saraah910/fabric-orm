/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;

@DataType()
public class Asset {

    private EntityManager manager;

    public void setEntityManager(EntityManager manager) {
        this.manager = manager;
    }

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

    @JsonIgnore()
    private Owner owner;

    public Owner getOwner() {
        if (owner == null) {
            owner = manager.loadOwner(ownerID);
        }
        return owner;
    }

    public void setAssetID(String assetID) {
        this.assetID = assetID;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setAppraisedValue(int appraisedValue) {
        this.appraisedValue = appraisedValue;
    }
    public void setOwner(Owner newOwner) {
        if (ownerID != null && manager != null) {
            Owner oldOwner = manager.loadOwner(ownerID);
            oldOwner.removeAssetID(assetID);
            manager.saveOwner(oldOwner);
            this.ownerID = newOwner.getOwnerID();
            manager.saveAsset(this);
            newOwner.addAssetID(assetID);
            manager.saveOwner(newOwner);            
        }
    }

    @JsonProperty("assetID")
    public String getAssetID() {
        return this.assetID;
    }

    @JsonProperty("color")
    public String getColor() {
        return this.color;
    }

    @JsonProperty("size")
    public int getSize() {
        return this.size;
    }

    @JsonProperty("ownerID")
    public String getOwnerID() {
        return this.ownerID;
    }

    @JsonProperty("AppraisedValue")
    public int getAppraisedValue() {
        return this.appraisedValue;
    }

    public Asset(@JsonProperty("assetID") final String assetID, @JsonProperty("color") final String color,
                 @JsonProperty("size") final int size, @JsonProperty("ownerID") final String ownerID,
                 @JsonProperty("AppraisedValue") final int appraisedValue) {
        this.assetID = assetID;
        this.color = color;
        this.size = size;
        this.ownerID = ownerID;
        this.appraisedValue = appraisedValue;
        this.owner = null;
    }
}
