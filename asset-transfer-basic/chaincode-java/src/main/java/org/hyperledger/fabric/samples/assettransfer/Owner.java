/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.Objects;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.Context;

@DataType()
public final class Owner{
    private final Genson genson = new Genson();
    
    transient public EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Property()
    private String ownerID;

    @Property()
    private String name;

    @Property()
    private String lastName;

    @Property()
    private ArrayList<String> OwnedAssetIDs;

    @Property()
    private ArrayList<Asset> ownedAssets = null;

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

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void addAssetIDs (final String assetID) {
        this.OwnedAssetIDs.add(assetID);
    }
    
    public void RemoveAssetID(final String assetID) {
        this.OwnedAssetIDs.remove(assetID);        
    }
    
    public ArrayList<Asset> getOwnedAssetsOfOwner() {      
        if (ownedAssets == null) {
            for (String assetID : OwnedAssetIDs) {
                Asset asset = entityManager.loadAssetFromLedger(assetID);
                OwnedAssetList.add(asset);
            }
        }      
        return OwnedAssetList;        
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
                @JsonProperty("ownedAssets") ArrayList<Asset> ownedAssets) {
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
                new String[] {other.getOwnerID(), other.getName(), other.getLastName()});
                    
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOwnerID(), getName(), getLastName());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [ownerID=" + ownerID + ", name="
                + name + ", lastName=" + lastName + ", OwnedAssetIDs= "+ OwnedAssetIDs + ", OwnedAssets= "+ null +"]";
    }


}