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

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;


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
    private ArrayList<String> ownedAssetIDs;

    @Property()
    private ObservableList<Asset> ownedAssets;

    public void setName(String newName) {
        this.name = newName;
        propertyChangeSupport.firePropertyChange("name",null,this.name);
    }

    public void setLastName(String newLastName) {
        this.lastName = newLastName;
        propertyChangeSupport.firePropertyChange("lastName", null, this.lastName);
    }

    public void addAsset(Asset asset) {
        addAssetID(asset.getAssetID());
    }

    public void removeAsset(Asset asset) {
        removeAssetID(asset.getAssetID());
    }

    private void addAssetID(String assetID) {
        this.ownedAssetIDs.add(assetID);
        propertyChangeSupport.firePropertyChange("OwnedAssetIDCollection", null, assetID);
    }

    private void removeAssetID(String assetID) {
        this.ownedAssetIDs.remove(assetID);
        propertyChangeSupport.firePropertyChange("OwnedAssetIDCollection", null, assetID);
    }

    public ObservableList<Asset> GetOwnedAssets() {
        ObservableList<Asset> OwnedAssetList = FXCollections.observableArrayList();
            
            System.out.println("populating assets");
            for (String assetID: ownedAssetIDs) {
                Asset asset = manager.loadAsset(assetID);
                OwnedAssetList.add(asset);
            }        
        return OwnedAssetList;
    }

    @JsonProperty("ownerID")
    public String getOwnerID() {
        return this.ownerID;
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
        return this.ownedAssetIDs;
    }

    @JsonProperty("OwnedAssetIDCollection")
    public void setMyAssetIDCollection(ArrayList<String> OwnedAssetIDCollection) {
        this.ownedAssetIDs = OwnedAssetIDCollection;
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
    
    private void initiateChange() {
        ownedAssets.addListener(new ListChangeListener<Asset>() {
        @Override
        public void onChanged(Change<? extends Asset> change) {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Asset asset : change.getAddedSubList()) {
                        System.out.println("Adding assetID: " + asset.getAssetID());
                        addAssetID(asset.getAssetID());
                    }
                } else if (change.wasRemoved()) {
                    for (Asset asset : change.getRemoved()) {
                        System.out.println("Removing assetID: " + asset.getAssetID());
                        removeAssetID(asset.getAssetID());
                    }
                }
            }
        }});
    }

    public Owner(String ownerID, String name, String lastName) {
        this.ownerID = ownerID;
        this.name = name;
        this.lastName = lastName;
        this.ownedAssetIDs = new ArrayList<>();
        this.ownedAssets = null;
    }

    public Owner(@JsonProperty("ownerID") final String ownerID, @JsonProperty("name") final String name,
                @JsonProperty("lastName") final String lastName, @JsonProperty("ownedAssets") final ObservableList<Asset> ownedAssets) {
        this.ownerID = ownerID;
        this.name = name;
        this.lastName = lastName;
        this.ownedAssets = FXCollections.observableArrayList();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        initiateChange();
    }

}