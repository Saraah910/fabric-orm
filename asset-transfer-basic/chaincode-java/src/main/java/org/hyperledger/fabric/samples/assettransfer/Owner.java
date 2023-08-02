/*
 * SPDX-License-Identifier: Apache-2.0
 */

 package org.hyperledger.fabric.samples.assettransfer;

 import java.util.ArrayList;
 import java.util.Objects;
 
 import org.hyperledger.fabric.contract.annotation.DataType;
 import org.hyperledger.fabric.contract.annotation.Property;
 
 import com.owlike.genson.annotation.JsonProperty;
 
 
 @DataType()
 public final class Owner {
 
     @Property()
     private final String ownerID;
 
     @Property()
     private final String name;
 
     @Property()
     private final String lastName;
 
     @Property()
     private final ArrayList<String> OwnedAssetIDs;
 
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
 
     public void addAssetIDs(final String assetID) {
         this.OwnedAssetIDs.add(assetID);
     }
 
     public Owner(final String ownerID, final String name, final String lastName) {
         this.ownerID = ownerID;
         this.name = name;
         this.lastName = lastName;
         this.OwnedAssetIDs = new ArrayList<String>();
     }
 
     public Owner(@JsonProperty("ownerID") final String ownerID, @JsonProperty("name") final String name,
             @JsonProperty("lastName") final String lastName, @JsonProperty("iDsOfOwnedAssets") final ArrayList<String> OwnedAssetIDs) {
         this.ownerID = ownerID;
         this.name = name;
         this.lastName = lastName;
         this.OwnedAssetIDs = OwnedAssetIDs;
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
                 Objects.deepEquals(getIDsOfOwnedAssets(), other.getIDsOfOwnedAssets());
                 
                 
     }
 
     @Override
     public int hashCode() {
         return Objects.hash(getOwnerID(), getName(), getLastName(), getIDsOfOwnedAssets());
     }
 
     @Override
     public String toString() {
         return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [ownerID=" + ownerID + ", name="
                 + name + ", lastName=" + lastName + "iDsOfOwnedAssets=" + OwnedAssetIDs + "]";
     }
 }
 