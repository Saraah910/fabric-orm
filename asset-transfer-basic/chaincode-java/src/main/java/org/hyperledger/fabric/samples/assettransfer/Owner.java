/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples.assettransfer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;


@DataType()
public final class Owner{

    private EntityManager manager;
    private PropertyChangeSupport propertyChangeSupport;
    public void setEntityManager(EntityManager manager) {
        this.manager = manager;
    }

    @Property()
    private String ownerID;

    @Property()
    private String name;

    @Property()
    private String lastName;

    @Property()
    private ArrayList<String> OwnedAssetIDCollection;

    @Property()
    private ArrayList<Asset> ownedAssets;

    private ArrayList<Asset> OwnedAssetList = new ArrayList<>();

    @JsonProperty("ownerID")
    public String getOwnerID() {
        return this.ownerID;
    }

    public void setName(String newName) {
        this.name = newName;
        propertyChangeSupport.firePropertyChange("name",null,this.name);
    }

    public void setLastName(String newLastName) {
        this.lastName = newLastName;
        propertyChangeSupport.firePropertyChange("lastName", null, this.lastName);
    }

    public void addAssetID(String assetID) {
        this.OwnedAssetIDCollection.add(assetID);
    }

    public void removeAssetID(String assetID) {
        this.OwnedAssetIDCollection.remove(assetID);
    }

    public ArrayList<Asset> grabAssetCollection() {
        if (ownedAssets == null) {
            for (String assetID: OwnedAssetIDCollection) {
                Asset asset = manager.loadAsset(assetID);
                OwnedAssetList.add(asset);
            }
        }
        return OwnedAssetList;
    }
    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return this.lastName;
    }

    @JsonProperty("OwnedAssetIDCollection")
    public ArrayList<String> getMyAssetIDCollection() {
        return this.OwnedAssetIDCollection;
    }

    @JsonProperty("OwnedAssetIDCollection")
    public void setMyAssetIDCollection(ArrayList<String> OwnedAssetIDCollection) {
        this.OwnedAssetIDCollection = OwnedAssetIDCollection;
    }

    @JsonProperty("AssetsCollection")
    public ArrayList<Asset> fetchAssetCollection() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void handleAssetUpdate(PropertyChangeEvent event) {
        System.out.println("Owner " + name + " " + lastName + " received notification:");
        System.out.println("Asset Property Name: " + event.getPropertyName());
        System.out.println("Changed Value: " + event.getNewValue());
        System.out.println("-------------------");
    }
    public Owner(@JsonProperty("ownerID") final String ownerID, @JsonProperty("name") final String name,
                @JsonProperty("lastName") final String lastName) {
        this.ownerID = ownerID;
        this.name = name;
        this.lastName = lastName;
        this.OwnedAssetIDCollection = new ArrayList<String>();
        this.ownedAssets = null;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

}